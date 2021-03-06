package systemtests;

import static org.junit.Assert.assertFalse;
import static pwe.planner.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static pwe.planner.commons.core.Messages.MESSAGE_MODULES_LISTED_OVERVIEW;
import static pwe.planner.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static pwe.planner.logic.parser.BooleanExpressionParser.MESSAGE_EMPTY_OUTPUT;
import static pwe.planner.logic.parser.BooleanExpressionParser.MESSAGE_GENERAL_FAIL;
import static pwe.planner.logic.parser.BooleanExpressionParser.MESSAGE_INVALID_EXPRESSION;
import static pwe.planner.logic.parser.BooleanExpressionParser.MESSAGE_INVALID_OPERATOR_APPLICATION;
import static pwe.planner.logic.parser.CliSyntax.OPERATOR_AND;
import static pwe.planner.logic.parser.CliSyntax.OPERATOR_LEFT_BRACKET;
import static pwe.planner.logic.parser.CliSyntax.OPERATOR_OR;
import static pwe.planner.logic.parser.CliSyntax.OPERATOR_RIGHT_BRACKET;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CODE;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CREDITS;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_NAME;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_SEMESTER;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_TAG;
import static pwe.planner.testutil.TypicalModules.ALICE;
import static pwe.planner.testutil.TypicalModules.BENSON;
import static pwe.planner.testutil.TypicalModules.CARL;
import static pwe.planner.testutil.TypicalModules.DANIEL;
import static pwe.planner.testutil.TypicalModules.ELLE;
import static pwe.planner.testutil.TypicalModules.KEYWORD_MATCHING_MEIER;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pwe.planner.commons.core.index.Index;
import pwe.planner.logic.commands.DeleteCommand;
import pwe.planner.logic.commands.FindCommand;
import pwe.planner.logic.commands.RedoCommand;
import pwe.planner.logic.commands.UndoCommand;
import pwe.planner.model.Model;
import pwe.planner.model.tag.Tag;

public class FindCommandSystemTest extends ApplicationSystemTest {

    @Test
    public void find() {
        /* Case: find multiple modules in application, command with leading spaces and trailing spaces
         * -> 2 modules found
         */
        String command = "   " + FindCommand.COMMAND_WORD + " " + KEYWORD_MATCHING_MEIER + "   ";
        Model expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, BENSON, DANIEL); // first names of Benson and Daniel are "Meier"
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: repeat previous find command where module list is displaying the modules we are finding
         * -> 2 modules found
         */
        command = FindCommand.COMMAND_WORD + " " + KEYWORD_MATCHING_MEIER;
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module where module list is not displaying the module we are finding -> 1 module found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Carl";
        ModelHelper.setFilteredList(expectedModel, CARL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find multiple modules in application, 2 keywords -> 2 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Benson " + OPERATOR_OR + " "
                + PREFIX_NAME + "Daniel";
        ModelHelper.setFilteredList(expectedModel, BENSON, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find multiple modules in application, 2 keywords in reversed order -> 2 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " "
                + PREFIX_NAME + "Benson";
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find multiple modules in application, 2 keywords with 1 repeat -> 2 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " "
                + PREFIX_NAME + "Benson " + OPERATOR_OR + " " + PREFIX_NAME + "Daniel";
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find multiple modules in application, 2 matching keywords and 1 non-matching keyword
         * -> 2 modules found
         */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " "
                + PREFIX_NAME + "Benson " + OPERATOR_OR + " " + PREFIX_NAME + "NonMatchingKeyWord";
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: undo previous find command -> rejected */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_FAILURE;
        assertCommandFailure(command, expectedResultMessage);

        /* Case: redo previous find command -> rejected */
        command = RedoCommand.COMMAND_WORD;
        expectedResultMessage = RedoCommand.MESSAGE_FAILURE;
        assertCommandFailure(command, expectedResultMessage);

        /* Case: find same modules in application after deleting 1 of them -> 1 module found */
        executeCommand(DeleteCommand.COMMAND_WORD + " 1");
        assertFalse(getModel().getApplication().getModuleList().contains(BENSON));
        command = FindCommand.COMMAND_WORD + " " + KEYWORD_MATCHING_MEIER;
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in application, keyword is same as name but of different case -> 1 module found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "MeIeR";
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in application, keyword is substring of name -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Mei";
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in application, name is substring of keyword -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Meiers";
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module not in application -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Mark";
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find credits of module in application -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + DANIEL.getCredits().value;
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find credits of module in application with PREFIX_CREDITS -> 1 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CREDITS + DANIEL.getCredits().value;
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find credits of module not in application with PREFIX_CREDITS -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CREDITS + "963";
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in application, credits is substring of keyword -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CREDITS + "999";
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find code of module in application -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + DANIEL.getCode().value;
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        // TODO: Update the test case again after proper attribute is given in TypicalModules
        /* Case: find code of module in application with correct PREFIX -> 1 module found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CODE + DANIEL.getCode().value;
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find non-existent module in application with PREFIX_CODE -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CODE + "AAA1234Z";
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in application, code is substring of keyword -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CODE + "FS4205"; // valid partial code derived from IFS4205
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find code of module in application with correct PREFIX -> 3 module found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_TAG + "friends";
        ModelHelper.setFilteredList(expectedModel, ALICE, BENSON, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find non-existent module in application with PREFIX_TAG -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_TAG + "NotExisting";
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in application with PREFIX_TAG, keyword is substring of a valid tag -> 0 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_TAG + "frie"; // derived from friends
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find tags of module in application -> 0 modules found */
        List<Tag> tags = new ArrayList<>(DANIEL.getTags());
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + tags.get(0).tagName;
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find module in the application with PREFIX_SEM/4 -> 2 modules found */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_SEMESTER + "4";
        ModelHelper.setFilteredList(expectedModel, ALICE);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: find while a module is selected -> selected card deselected */
        showAllModules();
        selectModule(Index.fromOneBased(1));
        assertFalse(getModuleListPanel().getHandleToSelectedCard().getName().equals(DANIEL.getName().fullName));
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel";
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardDeselected();

        /* Case: find module in empty application -> 0 modules found */
        deleteAllModules();
        command = FindCommand.COMMAND_WORD + " " + KEYWORD_MATCHING_MEIER;
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);
        assertSelectedCardUnchanged();

        /* Case: mixed case command word -> rejected */
        command = "FiNd " + PREFIX_NAME + "Meier";
        assertCommandFailure(command, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void multiFind() {
        /* Case: find module with name daniel, code 'CS1231' and credits '2'-> 3 modules return */
        String command =
                FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " " + PREFIX_CODE + "CS1231 "
                        + OPERATOR_OR + " " + PREFIX_CREDITS + "2";
        Model expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL, BENSON, CARL);
        assertCommandSuccess(command, expectedModel);

        /* Case: find module with name, code and credits in different order -> 3 modules return */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CODE + "CS1231 " + OPERATOR_OR + " "
                + PREFIX_CREDITS + "2 " + OPERATOR_OR + " " + PREFIX_NAME + "Daniel ";
        assertCommandSuccess(command, expectedModel);

        /* Case: find module with name daniel, credits '95352563' and invalid code -> 2 modules return */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " "
                + PREFIX_CODE + "AZ0000 " + OPERATOR_OR + " " + PREFIX_CREDITS + "2";
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL, CARL);
        assertCommandSuccess(command, expectedModel);

        /* Case: find module with valid name, code and non-existent credits -> 2 modules return */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " "
                + PREFIX_CODE + "CS1231 " + OPERATOR_OR + " " + PREFIX_CREDITS + "968";
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL, BENSON);
        assertCommandSuccess(command, expectedModel);

        /* Case: find module with valid name but non-existent code and credits -> 1 modules return */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_OR + " "
                + PREFIX_CODE + "FAS1234 " + OPERATOR_OR + " " + PREFIX_CREDITS + "999";
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);

        /* Case: find module with non-existent name, code and credits -> 0 modules return */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Programmmmming " + OPERATOR_OR + " "
                + PREFIX_CODE + "FAS1234 " + OPERATOR_OR + " " + PREFIX_CREDITS + "999";
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);
    }

    @Test
    public void multiBooleanAndFind() {

        /* Case: Find module name which contain both Daniel and Meier -> Return exactly 1 module
          e.g. find ( name/Daniel && name/Meier )
        */
        String command = FindCommand.COMMAND_WORD + " ( " + PREFIX_NAME + "Daniel " + OPERATOR_AND + " "
                + PREFIX_NAME + "Meier )";
        Model expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL);
        assertCommandSuccess(command, expectedModel);

        /* Case: Find module name which contain both Daniel and Meier -> Return exactly 1 module
          e.g. find name/Daniel && name/Meier
        */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "Daniel " + OPERATOR_AND + " "
                + PREFIX_NAME + "Meier";
        expectedModel = getModel();
        assertCommandSuccess(command, expectedModel);

        /* Case: Find module code which contain CS1231 and CS2100 -> Return exactly 0 module */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CODE + "CS2100 " + OPERATOR_AND + " " + PREFIX_CODE
                + "CS1231";
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);

        /* Case: Find module credits that are 4 and 0 -> Return exactly 0 module */
        command = FindCommand.COMMAND_WORD + " " + PREFIX_CREDITS + "4 " + OPERATOR_AND + " " + PREFIX_CREDITS
                + "0";
        expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel);
        assertCommandSuccess(command, expectedModel);

        command = FindCommand.COMMAND_WORD + " " + PREFIX_SEMESTER + "4" + OPERATOR_AND + " " + PREFIX_SEMESTER
                + "2";
        ModelHelper.setFilteredList(expectedModel, ALICE);
        assertCommandSuccess(command, expectedModel);
    }

    @Test
    public void complexMultiFind() {
        /*  find code/CS2102 || ( name/Daniel && name/Meier )  -> Return 2 modules */
        String command =
                FindCommand.COMMAND_WORD + " " + PREFIX_CODE + "CS2040C" + " " + OPERATOR_OR + " "
                        + OPERATOR_LEFT_BRACKET + PREFIX_NAME + "Daniel " + OPERATOR_AND + " "
                        + PREFIX_NAME + "Meier " + OPERATOR_RIGHT_BRACKET;
        Model expectedModel = getModel();
        ModelHelper.setFilteredList(expectedModel, DANIEL, CARL);
        assertCommandSuccess(command, expectedModel);

        /* find (sem/1 || sem/2) && credits/4 -> Return 1 module */
        command = FindCommand.COMMAND_WORD + " " + OPERATOR_LEFT_BRACKET + PREFIX_SEMESTER + "1" + OPERATOR_OR
                + PREFIX_SEMESTER + "2" + OPERATOR_RIGHT_BRACKET + OPERATOR_AND + PREFIX_CREDITS + "4";
        ModelHelper.setFilteredList(expectedModel, ELLE);
        assertCommandSuccess(command, expectedModel);
    }

    @Test
    public void negativeTest() {
        // invalid operator
        String command = FindCommand.COMMAND_WORD + " name/Programming !! code/CS1231";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_EXPRESSION, MESSAGE_INVALID_OPERATOR_APPLICATION));
        // invalid operator
        command = FindCommand.COMMAND_WORD + " name/Programming ## code/CS1231";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_EXPRESSION, MESSAGE_INVALID_OPERATOR_APPLICATION));

        // missing operator
        command = FindCommand.COMMAND_WORD + " name/Programming code/CS1231";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_EXPRESSION, MESSAGE_INVALID_OPERATOR_APPLICATION));

        // valid + invalid prefix
        command = FindCommand.COMMAND_WORD + " name/Programming " + OPERATOR_OR + " nonExisting/CS1231";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        // single invalid prefix
        command = FindCommand.COMMAND_WORD + " nonExisting/CS1231";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        // single invalid prefix with multiple white space
        command = FindCommand.COMMAND_WORD + "                          nonExisting/CS1231                ";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));

        // empty criteria with ()
        command = FindCommand.COMMAND_WORD + " ()";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_EXPRESSION, MESSAGE_EMPTY_OUTPUT));

        command = FindCommand.COMMAND_WORD + " (name/Programming))";
        assertCommandFailure(command, String.format(MESSAGE_INVALID_EXPRESSION, MESSAGE_GENERAL_FAIL));


    }

    /**
     * Executes {@code command} and verifies that the command box displays an empty string, the result display
     * box displays {@code Messages#MESSAGE_MODULES_LISTED_OVERVIEW} with the number of modules in the filtered list,
     * and the model related components equal to {@code expectedModel}.
     * These verifications are done by
     * {@code ApplicationSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * Also verifies that the status bar remains unchanged, and the command box has the default style class, and the
     * selected card updated accordingly, depending on {@code cardStatus}.
     * @see ApplicationSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandSuccess(String command, Model expectedModel) {
        String expectedResultMessage = String.format(
                MESSAGE_MODULES_LISTED_OVERVIEW, expectedModel.getFilteredModuleList().size());

        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchanged();
    }

    /**
     * Executes {@code command} and verifies that the command box displays {@code command}, the result display
     * box displays {@code expectedResultMessage} and the model related components equal to the current model.
     * These verifications are done by
     * {@code ApplicationSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * Also verifies that the browser url, selected card and status bar remain unchanged, and the command box has the
     * error style.
     * @see ApplicationSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
