package pwe.planner.ui.testutil;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import guitests.guihandles.ModuleCardHandle;
import guitests.guihandles.ModuleListPanelHandle;
import guitests.guihandles.ResultDisplayHandle;
import pwe.planner.model.module.Module;

/**
 * A set of assertion methods useful for writing GUI tests.
 */
public class GuiTestAssert {
    /**
     * Asserts that {@code actualCard} displays the same values as {@code expectedCard}.
     */
    public static void assertCardEquals(ModuleCardHandle expectedCard, ModuleCardHandle actualCard) {
        assertEquals(expectedCard.getId(), actualCard.getId());
        assertEquals(expectedCard.getCode(), actualCard.getCode());
        assertEquals(expectedCard.getName(), actualCard.getName());
        assertEquals(expectedCard.getCredits(), actualCard.getCredits());
        assertEquals(expectedCard.getTags(), actualCard.getTags());
        assertEquals(expectedCard.getCorequisites(), actualCard.getCorequisites());
    }

    /**
     * Asserts that {@code actualCard} displays the details of {@code expectedModule}.
     */
    public static void assertCardDisplaysModule(Module expectedModule, ModuleCardHandle actualCard) {
        assertEquals(expectedModule.getName().fullName, actualCard.getName());
        assertEquals(expectedModule.getCredits().value, actualCard.getCredits());
        assertEquals(expectedModule.getCode().value, actualCard.getCode());
        assertEquals(expectedModule.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.toList()),
                actualCard.getTags());
    }

    /**
     * Asserts that the list in {@code moduleListPanelHandle} displays the details of {@code modules} correctly and
     * in the correct order.
     */
    public static void assertListMatching(ModuleListPanelHandle moduleListPanelHandle, Module... modules) {
        for (int i = 0; i < modules.length; i++) {
            moduleListPanelHandle.navigateToCard(i);
            assertCardDisplaysModule(modules[i], moduleListPanelHandle.getModuleCardHandle(i));
        }
    }

    /**
     * Asserts that the list in {@code moduleListPanelHandle} displays the details of {@code modules} correctly and
     * in the correct order.
     */
    public static void assertListMatching(ModuleListPanelHandle moduleListPanelHandle, List<Module> modules) {
        assertListMatching(moduleListPanelHandle, modules.toArray(new Module[0]));
    }

    /**
     * Asserts the size of the list in {@code moduleListPanelHandle} equals to {@code size}.
     */
    public static void assertListSize(ModuleListPanelHandle moduleListPanelHandle, int size) {
        int numberOfModules = moduleListPanelHandle.getListSize();
        assertEquals(size, numberOfModules);
    }

    /**
     * Asserts the message shown in {@code resultDisplayHandle} equals to {@code expected}.
     */
    public static void assertResultMessage(ResultDisplayHandle resultDisplayHandle, String expected) {
        assertEquals(expected, resultDisplayHandle.getText());
    }
}
