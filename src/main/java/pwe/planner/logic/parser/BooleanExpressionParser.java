package pwe.planner.logic.parser;

import static pwe.planner.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static pwe.planner.commons.util.CollectionUtil.requireAllNonNull;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CODE;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CREDITS;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_NAME;
import static pwe.planner.logic.parser.Operator.applyOperator;
import static pwe.planner.logic.parser.Operator.getOperatorFromString;
import static pwe.planner.logic.parser.ParserUtil.parseCode;
import static pwe.planner.logic.parser.ParserUtil.parseCredits;
import static pwe.planner.logic.parser.ParserUtil.parseName;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import pwe.planner.logic.commands.FindCommand;
import pwe.planner.logic.parser.exceptions.ParseException;
import pwe.planner.model.module.CodeContainsKeywordsPredicate;
import pwe.planner.model.module.CreditsContainsKeywordsPredicate;
import pwe.planner.model.module.KeywordsPredicate;
import pwe.planner.model.module.Module;
import pwe.planner.model.module.NameContainsKeywordsPredicate;

/**
 * Parse input string into a composite predicate.
 */
public class BooleanExpressionParser {

    private static final String WHITESPACE = " ";

    private static KeywordsPredicate getKeywordsPredicate(String args) throws ParseException {
        assert args != null;

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_CODE, PREFIX_CREDITS);
        KeywordsPredicate predicate = null;
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            String nameKeyword = parseName(argMultimap.getValue(PREFIX_NAME).get()).toString();
            predicate = new NameContainsKeywordsPredicate(List.of(nameKeyword));
        } else if (argMultimap.getValue(PREFIX_CODE).isPresent()) {
            String codeKeyword = parseCode(argMultimap.getValue(PREFIX_CODE).get()).toString();
            predicate = new CodeContainsKeywordsPredicate(List.of(codeKeyword));
        } else if (argMultimap.getValue(PREFIX_CREDITS).isPresent()) {
            String creditKeyword = parseCredits(argMultimap.getValue(PREFIX_CREDITS).get()).toString();
            predicate = new CreditsContainsKeywordsPredicate(List.of(creditKeyword));
        } else {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return predicate;
    }

    /**
     * Parse input argument into a composite predicate.
     * This parse method make use of the shunting yard algorithm to convert in-fix to post fix then evaluate
     * the expression.
     *
     * @param stringToTokenize the user provided argument
     * @return a composite predicate
     */
    public static Predicate<Module> parse(String stringToTokenize, List<Prefix> prefixes) throws ParseException {
        requireAllNonNull(stringToTokenize, prefixes);

        BooleanExpressionTokenizer tokenizer = new BooleanExpressionTokenizer(stringToTokenize, prefixes);

        Deque<Predicate<Module>> output = new ArrayDeque<>();
        Deque<Operator> operatorStack = new ArrayDeque<>();

        try {
            boolean isNotExpectingLeftBracket = false;
            while (tokenizer.hasMoreTokens()) {
                String currentToken = tokenizer.nextToken();
                switch (currentToken) {
                case CliSyntax.OPERATOR_LEFT_BRACKET:
                    if (isNotExpectingLeftBracket) {
                        throw new ParseException(
                                String.format(FindCommand.MESSAGE_INVALID_EXPRESSION, FindCommand.MESSAGE_USAGE));
                    } else {
                        operatorStack.push(Operator.LEFT_BRACKET);
                        isNotExpectingLeftBracket = false;
                    }
                    break;
                case CliSyntax.OPERATOR_RIGHT_BRACKET:
                    while (operatorStack.peek() != Operator.LEFT_BRACKET) {
                        output.push(applyOperator(operatorStack.pop(), output.pop(), output.pop()));
                    }
                    operatorStack.pop();
                    isNotExpectingLeftBracket = true;
                    break;
                case CliSyntax.OPERATOR_OR: // Fallthrough
                case CliSyntax.OPERATOR_AND:
                    while (!operatorStack.isEmpty()
                            && Operator.hasHigherPrecedence(operatorStack.peek(),
                            getOperatorFromString(currentToken))) {
                        output.push(applyOperator(operatorStack.pop(), output.pop(), output.pop()));
                    }
                    operatorStack.push(getOperatorFromString(currentToken));
                    isNotExpectingLeftBracket = false;
                    break;
                default:
                    // as ArgumentMultimap require a whitespace before the args
                    // we will have to add a whitespace before our args without changing the code
                    // of ArgumentMultimap.
                    Predicate<Module> in = getKeywordsPredicate(WHITESPACE + currentToken);
                    output.push(in);
                    isNotExpectingLeftBracket = false;
                    break;
                }
            }
        } catch (NoSuchElementException nse) {
            throw new ParseException(String.format(FindCommand.MESSAGE_INVALID_EXPRESSION, FindCommand.MESSAGE_USAGE));
        }

        while (!operatorStack.isEmpty()) {
            // need at least 2 inputs predicates to apply operator
            if (output.size() >= 2) {
                output.push(applyOperator(operatorStack.pop(), output.pop(), output.pop()));
            } else {
                throw new ParseException(
                        String.format(FindCommand.MESSAGE_INVALID_EXPRESSION, FindCommand.MESSAGE_USAGE));
            }
        }

        // Output stack cannot have more than 1 predicate after shunting yard.
        // i.e. There is 2 predicate in an expression without a operator.
        if (output.size() > 1) {
            throw new ParseException(String.format(FindCommand.MESSAGE_INVALID_EXPRESSION, FindCommand.MESSAGE_USAGE));
        }

        assert output.size() == 1 : "output.size() should be 1.";
        return output.pop();
    }
}
