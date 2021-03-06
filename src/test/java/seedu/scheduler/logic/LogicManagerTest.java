package seedu.scheduler.logic;

import com.google.common.eventbus.Subscribe;

import guitests.GuiRobot;
import javafx.scene.input.KeyCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import seedu.scheduler.commons.core.EventsCenter;
import seedu.scheduler.logic.commands.*;
import seedu.scheduler.commons.events.ui.ShowHelpRequestEvent;
import seedu.scheduler.commons.events.model.SchedulerChangedEvent;
import seedu.scheduler.model.Scheduler;
import seedu.scheduler.model.Model;
import seedu.scheduler.model.ModelManager;
import seedu.scheduler.model.ReadOnlyScheduler;
import seedu.scheduler.model.entry.*;
import seedu.scheduler.model.entry.UniqueEntryList.DuplicateEntryException;
import seedu.scheduler.model.tag.Tag;
import seedu.scheduler.model.tag.UniqueTagList;
import seedu.scheduler.storage.StorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static seedu.scheduler.commons.core.Messages.*;

public class LogicManagerTest {

    /**
     * See https://github.com/junit-team/junit4/wiki/rules#temporaryfolder-rule
     */
    @Rule
    public TemporaryFolder saveFolder = new TemporaryFolder();

    private Model model;
    private Logic logic;

    //These are for checking the correctness of the events raised
    private ReadOnlyScheduler latestSavedScheduler;
    private boolean helpShown;

    @Subscribe
    private void handleLocalModelChangedEvent(SchedulerChangedEvent abce) {
        latestSavedScheduler = new Scheduler(abce.data);
    }

    @Subscribe
    private void handleShowHelpRequestEvent(ShowHelpRequestEvent she) {
        helpShown = true;
    }

    @Before
    public void setup() {
        model = new ModelManager();
        String tempSchedulerFile = saveFolder.getRoot().getPath() + "TempScheduler.xml";
        String tempPreferencesFile = saveFolder.getRoot().getPath() + "TempPreferences.json";
        logic = new LogicManager(model, new StorageManager(tempSchedulerFile, tempPreferencesFile));
        EventsCenter.getInstance().registerHandler(this);

        latestSavedScheduler = new Scheduler(model.getScheduler()); // last saved assumed to be up to date before.
        helpShown = false;
    }

    @After
    public void teardown() {
        EventsCenter.clearSubscribers();
    }

    @Test
    public void execute_invalid() throws Exception {
        String invalidCommand = "       ";
        assertCommandBehavior(invalidCommand,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    /**
     * Executes the command and confirms that the result message is correct.
     * Both the 'scheduler' and the 'last shown list' are expected to be empty.
     * @see #assertCommandBehavior(String, String, ReadOnlyScheduler, List)
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage) throws Exception {
        assertCommandBehavior(inputCommand, expectedMessage, new Scheduler(), Collections.emptyList());
    }

    /**
     * Executes the command and confirms that the result message is correct and
     * also confirms that the following three parts of the LogicManager object's state are as expected:<br>
     *      - the internal scheduler data are same as those in the {@code expectedScheduler} <br>
     *      - the backing list shown by UI matches the {@code shownList} <br>
     *      - {@code expectedScheduler} was saved to the storage file. <br>
     */
    private void assertCommandBehavior(String inputCommand, String expectedMessage,
                                       ReadOnlyScheduler expectedScheduler,
                                       List<? extends ReadOnlyEntry> expectedShownList) throws Exception {

        //Execute the command
        CommandResult result = logic.execute(inputCommand);
        if (inputCommand.equals("clear")) {
            GuiRobot robot = new GuiRobot();
            robot.type(KeyCode.ENTER).sleep(500);
        }
        //Confirm the ui display elements should contain the right data
        if(!inputCommand.equals("undo") && !inputCommand.equals("redo")) {
            assertEquals(expectedMessage, result.feedbackToUser);
        }
        assertEquals(expectedShownList, model.getFilteredEntryList());

        //Confirm the state of data (saved and in-memory) is as expected
        assertEquals(expectedScheduler, model.getScheduler());
        assertEquals(expectedScheduler, latestSavedScheduler);
    }


    @Test
    public void execute_unknownCommandWord() throws Exception {
        String unknownCommand = "uicfhmowqewca";
        assertCommandBehavior(unknownCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_help() throws Exception {
        assertCommandBehavior("help", HelpCommand.SHOWING_HELP_MESSAGE);
        assertTrue(helpShown);
    }

    @Test
    public void execute_exit() throws Exception {
        assertCommandBehavior("exit", ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);
    }

    @Test
    public void execute_clear() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        model.addEntry(helper.generateEntry(1));
        model.addEntry(helper.generateEntry(2));
        model.addEntry(helper.generateEntry(3));
        //NEEDFIX
        //assertCommandBehavior("clear", ClearCommand.MESSAGE_SUCCESS, new Scheduler(), Collections.emptyList());
    }
    
    //@@author A0152962B
    @Test
    public void execute_clearUndoRedo() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        List<Entry> threeEntrys = helper.generateEntryList(3);
        Scheduler expectedAB = helper.generateScheduler(threeEntrys);
        
        // prepare scheduler
        helper.addToModel(model, threeEntrys);
        //NEEDFIX 
        //assertCommandBehavior("clear", ClearCommand.MESSAGE_SUCCESS, new Scheduler(), Collections.emptyList());

        //assertCommandBehavior("undo", ClearCommand.MESSAGE_SUCCESS, expectedAB, expectedAB.getEntryList());

        //assertCommandBehavior("redo", ClearCommand.MESSAGE_SUCCESS, new Scheduler(), Collections.emptyList());
    }
    
    //@@author
    @Test
    public void execute_add_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertCommandBehavior(
                "a test g/1231", expectedMessage);
    }

    //@@author A0126090N
    @Test
    public void execute_add_invalidEntryData() throws Exception {
        assertCommandBehavior(
                "add []\\[;]", Name.MESSAGE_NAME_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name et/01:02 st/01:02", String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        assertCommandBehavior(
                "add Valid Name st/25:02", StartTime.MESSAGE_START_TIME_CONSTRAINTS);
    }
    //@@author
    
    @Test
    public void execute_add_successful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Entry toBeAdded = helper.adam();
        Scheduler expectedAB = new Scheduler();
        expectedAB.addEntry(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList());

    }

    @Test
    public void execute_addDuplicate_notAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Entry toBeAdded = helper.adam();
        Scheduler expectedAB = new Scheduler();
        expectedAB.addEntry(toBeAdded);

        // setup starting state
        model.addEntry(toBeAdded); // entry already in internal scheduler

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddCommand(toBeAdded),
                AddCommand.MESSAGE_DUPLICATE_ENTRY,
                expectedAB,
                expectedAB.getEntryList());

    }
    
    //@@author A0152962B
    @Test
    public void execute_addUndoRedo() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Entry toBeAdded = helper.adam();
        List<Entry> threeEntrys = helper.generateEntryList(3);
        Scheduler expectedAB = helper.generateScheduler(threeEntrys);
        expectedAB.addEntry(toBeAdded);
        
        // prepare scheduler
        helper.addToModel(model, threeEntrys);
        
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList());
        
        // prepare expectedAB for undo
        expectedAB.removeEntry(toBeAdded);
        
        assertCommandBehavior("undo",
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList());
        
        // prepare expectedAB for redo
        expectedAB.addEntry(toBeAdded);
        
        assertCommandBehavior("redo",
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList());
    }
    
    //@@author
    @Test
    public void execute_list_showsAllEntrys() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        Scheduler expectedAB = helper.generateScheduler(2);
        List<? extends ReadOnlyEntry> expectedList = expectedAB.getEntryList();

        // prepare scheduler state
        helper.addToModel(model, 2);
        //NEED TO FIX
        //assertCommandBehavior("list",
        //        ListCommand.MESSAGE_SUCCESS,
        //        expectedAB,
        //        expectedList);
    }


    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single entry in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single entry in the last shown list based on visible index.
     */
    private void assertIncorrectIndexFormatBehaviorForCommand(String commandWord, String expectedMessage) throws Exception {
        assertCommandBehavior(commandWord , expectedMessage); //index missing
        assertCommandBehavior(commandWord + " +1", expectedMessage); //index should be unsigned
        assertCommandBehavior(commandWord + " -1", expectedMessage); //index should be unsigned
        assertCommandBehavior(commandWord + " 0", expectedMessage); //index cannot be 0
        assertCommandBehavior(commandWord + " not_a_number", expectedMessage);
    }

    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single entry in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single entry in the last shown list based on visible index.
     */
    private void assertIndexNotFoundBehaviorForCommand(String commandWord) throws Exception {
        String expectedMessage = MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX;
        TestDataHelper helper = new TestDataHelper();
        List<Entry> entryList = helper.generateEntryList(2);

        // set AB state to 2 entrys
        model.resetData(new Scheduler());
        for (Entry p : entryList) {
            model.addEntry(p);
        }

        if(commandWord.equals("delete")){
            assertCommandBehavior(commandWord + " 3", expectedMessage, model.getScheduler(), entryList);
        }
        //@@author A0152962B
        else if(commandWord.equals("edit")){
            assertCommandBehavior(commandWord + " 3 name", expectedMessage, model.getScheduler(), entryList);
        }
        //@@author A0126090N
        else if(commandWord.equals("mark")){
            assertCommandBehavior(commandWord + " 3", expectedMessage, model.getScheduler(), entryList);
        }
        else if(commandWord.equals("m")){
            assertCommandBehavior(commandWord + " 3", expectedMessage, model.getScheduler(), entryList);
        }
    }

    //@@author A0126090N
    @Test
    public void execute_markedInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkedCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("mark", expectedMessage);
    }

    @Test
    public void execute_markedIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("mark");
    }
    
    @Test
    public void execute_markedAdvancedInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkedCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("m", expectedMessage);
    }

    @Test
    public void execute_markedAdvancedIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("m");
    }
    //@@author 
    
    @Test
    public void execute_delete_removesCorrectEntry() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Entry> threeEntrys = helper.generateEntryList(3);

        Scheduler expectedAB = helper.generateScheduler(threeEntrys);
        expectedAB.removeEntry(threeEntrys.get(1));
        helper.addToModel(model, threeEntrys);

        assertCommandBehavior("delete 2",
                String.format(DeleteCommand.MESSAGE_DELETE_ENTRY_SUCCESS, threeEntrys.get(1)),
                expectedAB,
                expectedAB.getEntryList());
    }
    
    @Test
    public void execute_deleteInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("delete", expectedMessage);
    }

    @Test
    public void execute_deleteIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("delete");
    }
  //@@author A0152962B
    @Test
    public void execute_deleteUndoRedo() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        
        List<Entry> threeEntrys = helper.generateEntryList(3);
        Entry toBeAdded = threeEntrys.get(1); // get second entry
        Scheduler expectedAB = helper.generateScheduler(threeEntrys);
        expectedAB.removeEntry(toBeAdded);
        
        // prepare scheduler
        helper.addToModel(model, threeEntrys);
        
        
        assertCommandBehavior("d 2",
                String.format(DeleteCommand.MESSAGE_DELETE_ENTRY_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList()); // delete second entry
        
        // prepare expectedAB for undo
        expectedAB.addEntryAtIndex(2, toBeAdded);
        
        assertCommandBehavior("undo",
                String.format(DeleteCommand.MESSAGE_DELETE_ENTRY_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList());
        
        // prepare expectedAB for redo
        expectedAB.removeEntry(toBeAdded);
        
        assertCommandBehavior("redo",
                String.format(DeleteCommand.MESSAGE_DELETE_ENTRY_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getEntryList());
    }
    
    @Test
    public void execute_editToDuplicate_notAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        List<Entry> threeEntrys = helper.generateEntryList(3);
        Entry toEditTo = helper.adam();
        threeEntrys.add(toEditTo);
        
        // prepare scheduler
        Scheduler expectedAB = helper.generateScheduler(threeEntrys);
        helper.addToModel(model, threeEntrys);
        
        assertCommandBehavior(helper.generateEditCommand(toEditTo, 2),
                String.format(EditCommand.MESSAGE_DUPLICATE_ENTRY, toEditTo), 
                expectedAB,
                expectedAB.getEntryList());
    }
    
    @Test
    public void execute_editIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("e");
    }
    
    @Test
    public void execute_editInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("edit asd", expectedMessage);
    }
    
    @Test
    public void execute_edit_editsCorrectEntry() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        List<Entry> threeEntrys = helper.generateEntryList(3);
        
        Scheduler expectedAB = helper.generateScheduler(threeEntrys);
        Entry toEditTo = helper.adam();
        expectedAB.editEntry(toEditTo, threeEntrys.get(1));
        
        // prepare scheduler
        helper.addToModel(model, threeEntrys);
        
        assertCommandBehavior(helper.generateEditCommand(toEditTo, 2), 
                String.format(EditCommand.MESSAGE_SUCCESS, toEditTo), 
                expectedAB,
                expectedAB.getEntryList());
    }
    //@@author

    @Test
    public void execute_find_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("find ", expectedMessage);
    }

    @Test
    public void execute_find_onlyMatchesFullWordsInNames() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Entry pTarget1 = helper.generateEntryWithName("bla bla KEY bla");
        Entry pTarget2 = helper.generateEntryWithName("bla KEY bla bceofeia");
        Entry p1 = helper.generateEntryWithName("KE Y");
        Entry p2 = helper.generateEntryWithName("KEYKEYKEY sduauo");

        List<Entry> fourEntrys = helper.generateEntryList(p1, pTarget1, p2, pTarget2);
        Scheduler expectedAB = helper.generateScheduler(fourEntrys);
        List<Entry> expectedList = helper.generateEntryList(pTarget1, pTarget2);
        helper.addToModel(model, fourEntrys);

        assertCommandBehavior("find KEY",
                Command.getMessageForEntryListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }
    
    //@@author A0161210A
    /**
     * Asserts find completed tasks displays correct entry list
     * 
     */
    @Test
    public void execute_find_matchesCompletedTasks() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Entry pTarget1 = helper.adam(); //adam is a completed task
        Entry p3 = helper.generateEntryWithName("bla KEY bla bceofeia");
        Entry p1 = helper.generateEntryWithName("KE Y");
        Entry p2 = helper.generateEntryWithName("KEYKEYKEY sduauo");
        

        List<Entry> fourEntrys = helper.generateEntryList(p1, pTarget1, p2, p3);
        Scheduler expectedAB = helper.generateScheduler(fourEntrys);
        List<Entry> expectedList = helper.generateEntryList(pTarget1);
        helper.addToModel(model, fourEntrys);

        assertCommandBehavior("find completed",
                Command.getMessageForEntryListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }
    /**
     * Asserts find incomplete tasks displays correct entry list
     * 
     */
    @Test
    public void execute_find_matchesIncompletedTasks() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Entry p1 = helper.adam(); //adam is a completed task
        Entry p1Target = helper.generateEntryWithName("bla KEY bla bceofeia");
        Entry p2Target = helper.generateEntryWithName("KE Y");
        Entry p3Target = helper.generateEntryWithName("KEYKEYKEY sduauo");
        

        List<Entry> fourEntrys = helper.generateEntryList(p1, p1Target, p2Target, p3Target);
        Scheduler expectedAB = helper.generateScheduler(fourEntrys);
        List<Entry> expectedList = helper.generateEntryList(p1Target, p2Target, p3Target);
        helper.addToModel(model, fourEntrys);

        assertCommandBehavior("find incompleted",
                Command.getMessageForEntryListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }
    //@@author

    @Test
    public void execute_find_isNotCaseSensitive() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Entry p1 = helper.generateEntryWithName("bla bla KEY bla");
        Entry p2 = helper.generateEntryWithName("bla KEY bla bceofeia");
        Entry p3 = helper.generateEntryWithName("key key");
        Entry p4 = helper.generateEntryWithName("KEy sduauo");

        List<Entry> fourEntrys = helper.generateEntryList(p3, p1, p4, p2);
        Scheduler expectedAB = helper.generateScheduler(fourEntrys);
        List<Entry> expectedList = fourEntrys;
        helper.addToModel(model, fourEntrys);

        assertCommandBehavior("find KEY",
                Command.getMessageForEntryListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_matchesIfAnyKeywordPresent() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Entry pTarget1 = helper.generateEntryWithName("bla bla KEY bla");
        Entry pTarget2 = helper.generateEntryWithName("bla rAnDoM bla bceofeia");
        Entry pTarget3 = helper.generateEntryWithName("key key");
        Entry p1 = helper.generateEntryWithName("sduauo");
        helper.adam();

        List<Entry> fourEntrys = helper.generateEntryList(pTarget1, p1, pTarget2, pTarget3);
        Scheduler expectedAB = helper.generateScheduler(fourEntrys);
        List<Entry> expectedList = helper.generateEntryList(pTarget1, pTarget2, pTarget3);
        helper.addToModel(model, fourEntrys);

        assertCommandBehavior("find key rAnDoM",
                Command.getMessageForEntryListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }


    /**
     * A utility class to generate test data.
     */
    class TestDataHelper{

        Entry adam() throws Exception {
            Name name = new Name("Adam Brown");
            StartTime privateStartTime = new StartTime("11:11");
            EndTime endTime = new EndTime("11:15");
            Date privateDate = new Date("01-02-2034");
            EndDate privateEndDate = new EndDate("01-02-2035");
            Tag tag1 = new Tag("Completed");
            Tag tag2 = new Tag("tag2");
            UniqueTagList tags = new UniqueTagList(tag1, tag2);
            return new Entry(name, privateStartTime, endTime, privateDate, privateEndDate, tags);
        }

        /**
         * Generates a valid entry using the given seed.
         * Running this function with the same parameter values guarantees the returned entry will have the same state.
         * Each unique seed will generate a unique Entry object.
         *
         * @param seed used to generate the entry data field values
         */
        Entry generateEntry(int seed) throws Exception {
            Random random = new Random();
            String[] num ={"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", 
                    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                    "20", "21", "22", "23", "24", "25", "26", "27", "28"};
            return new Entry(
                    new Name("Entry " + seed),
                    new StartTime(num[random.nextInt(24)] + ":00"),
                    new EndTime(num[random.nextInt(24)] + ":00"),
                    new Date(num[random.nextInt(28)+1] + "-" + num[random.nextInt(12)+1] + "-2016"),
                    new EndDate(num[random.nextInt(28)+1] + "-" + num[random.nextInt(12)+1] + "-2017"),
                    new UniqueTagList(new Tag("tag" + Math.abs(seed)), new Tag("tag" + Math.abs(seed + 1)))
            );
        }

        /** Generates the correct add command based on the entry given */
        String generateAddCommand(Entry e) {
            StringBuffer cmd = new StringBuffer();

            cmd.append("add ");

            cmd.append(e.getName().toString());
            cmd.append(" st/").append(e.getStartTime());
            cmd.append(" et/").append(e.getEndTime());
            cmd.append(" sd/").append(e.getDate());
            cmd.append(" ed/").append(e.getEndDate());
            
            UniqueTagList tags = e.getTags();
            for(Tag t: tags){
                cmd.append(" t/").append(t.tagName);
            }

            return cmd.toString();
        }
        
        //@@author A0152962B
        /** Generates the correct edit command based on the entry and index given */
        String generateEditCommand(Entry e, int index) {
            StringBuffer cmd = new StringBuffer();
            
            cmd.append("edit ");
            
            cmd.append(Integer.toString(index) + " ");
            cmd.append(e.getName().toString());
            cmd.append(" st/").append(e.getStartTime());
            cmd.append(" et/").append(e.getEndTime());
            cmd.append(" sd/").append(e.getDate());
            cmd.append(" ed/").append(e.getEndDate());
            
            UniqueTagList tags = e.getTags();
            for(Tag t: tags){
                cmd.append(" t/").append(t.tagName);
            }
            
            return cmd.toString();
        }
        //@@author

        /**
         * Generates an Scheduler with auto-generated entrys.
         */
        Scheduler generateScheduler(int numGenerated) throws Exception{
            Scheduler scheduler = new Scheduler();
            addToScheduler(scheduler, numGenerated);
            return scheduler;
        }

        /**
         * Generates an Scheduler based on the list of Entrys given.
         */
        Scheduler generateScheduler(List<Entry> entrys) throws Exception{
            Scheduler scheduler = new Scheduler();
            addToScheduler(scheduler, entrys);
            return scheduler;
        }

        /**
         * Adds auto-generated Entry objects to the given Scheduler
         * @param scheduler The Scheduler to which the Entrys will be added
         */
        void addToScheduler(Scheduler scheduler, int numGenerated) throws Exception{
            addToScheduler(scheduler, generateEntryList(numGenerated));
        }

        /**
         * Adds the given list of Entrys to the given Scheduler
         */
        void addToScheduler(Scheduler scheduler, List<Entry> entrysToAdd) throws Exception{
            for(Entry p: entrysToAdd){
                scheduler.addEntry(p);
            }
        }

        /**
         * Adds auto-generated Entry objects to the given model
         * @param model The model to which the Entrys will be added
         */
        void addToModel(Model model, int numGenerated) throws Exception{
            addToModel(model, generateEntryList(numGenerated));
        }

        /**
         * Adds the given list of Entrys to the given model
         */
        void addToModel(Model model, List<Entry> entrysToAdd) throws Exception{
            for(Entry p: entrysToAdd){
                model.addEntry(p);
            }
        }

        /**
         * Generates a list of Entrys based on the flags.
         */
        List<Entry> generateEntryList(int numGenerated) throws Exception{
            List<Entry> entrys = new ArrayList<>();
            for(int i = 1; i <= numGenerated; i++){
                entrys.add(generateEntry(i));
            }
            return entrys;
        }

        List<Entry> generateEntryList(Entry... entrys) {
            return Arrays.asList(entrys);
        }

        /**
         * Generates a Entry object with given name. Other fields will have some dummy values.
         */
        Entry generateEntryWithName(String name) throws Exception {
            return new Entry(
                    new Name(name),
                    new StartTime("11:11"),
                    new EndTime("11:11"),
                    new Date("01-02-2034"),
                    new EndDate("02-02-2034"),
                    new UniqueTagList(new Tag("tag"))
            );
        }
    }
}
