package systemtests;

import static pwe.planner.ui.testutil.GuiTestAssert.assertListMatching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import pwe.planner.model.Application;
import pwe.planner.model.module.Module;
import pwe.planner.model.util.SampleDataUtil;
import pwe.planner.testutil.TestUtil;

public class SampleDataTest extends ApplicationSystemTest {
    /**
     * Returns null to force test app to load data of the file in {@code getDataFileLocation()}.
     */
    @Override
    protected Application getInitialData() {
        return null;
    }

    /**
     * Returns a non-existent file location to force test app to load sample data.
     */
    @Override
    protected Path getModuleListFileLocation() {
        Path filePath = TestUtil.getFilePathInSandboxFolder("SomeModuleListFileThatDoesNotExist1234567890.xml");
        deleteFileIfExists(filePath);
        return filePath;
    }

    /**
     * Returns a non-existent file location to force test app to load sample data.
     */
    @Override
    protected Path getDegreePlannerListFileLocation() {
        Path filePath =
                TestUtil.getFilePathInSandboxFolder("SomeDegreePlannerListFileThatDoesNotExist1234567890.xml");
        deleteFileIfExists(filePath);
        return filePath;
    }

    /**
     * Returns a non-existent file location to force test app to load sample data.
     */
    @Override
    protected Path getRequirementCategoryListFileLocation() {
        Path filePath =
                TestUtil.getFilePathInSandboxFolder("SomeRequirementCategoryFileThatDoesNotExist1234567890.xml");
        deleteFileIfExists(filePath);
        return filePath;
    }

    /**
     * Deletes the file at {@code filePath} if it exists.
     */
    private void deleteFileIfExists(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ioe) {
            throw new AssertionError(ioe);
        }
    }

    @Test
    public void application_dataFileDoesNotExist_loadSampleData() {
        Module[] expectedList = SampleDataUtil.getSampleModules();
        assertListMatching(getModuleListPanel(), expectedList);
    }
}
