package seedu.address.logic;

import com.google.common.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import seedu.address.commons.core.EventsCenter;
import seedu.address.logic.commands.*;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.commons.events.ui.ShowHelpRequestEvent;
import seedu.address.commons.events.model.SchedulerChangedEvent;
import seedu.address.model.Scheduler;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyScheduler;
import seedu.address.model.person.*;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.storage.StorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static seedu.address.commons.core.Messages.*;

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
    private int targetedJumpIndex;

    @Subscribe
    private void handleLocalModelChangedEvent(SchedulerChangedEvent abce) {
        latestSavedScheduler = new Scheduler(abce.data);
    }

    @Subscribe
    private void handleShowHelpRequestEvent(ShowHelpRequestEvent she) {
        helpShown = true;
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent je) {
        targetedJumpIndex = je.targetIndex;
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
        targetedJumpIndex = -1; // non yet
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
                                       List<? extends ReadOnlyPerson> expectedShownList) throws Exception {

        //Execute the command
        CommandResult result = logic.execute(inputCommand);

        //Confirm the ui display elements should contain the right data
        assertEquals(expectedMessage, result.feedbackToUser);
        assertEquals(expectedShownList, model.getFilteredPersonList());

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
        model.addPerson(helper.generatePerson(1));
        model.addPerson(helper.generatePerson(2));
        model.addPerson(helper.generatePerson(3));

        assertCommandBehavior("clear", ClearCommand.MESSAGE_SUCCESS, new Scheduler(), Collections.emptyList());
    }


    @Test
    public void execute_add_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertCommandBehavior(
                "add wrong args wrong args", expectedMessage);
        assertCommandBehavior(
                "add Valid Name 12345 e/valid@endTime.butNoStartTimePrefix a/valid, address", expectedMessage);
        assertCommandBehavior(
                "add Valid Name sd/01-02-2015 valid@endTime.butNoPrefix a/valid, address", expectedMessage);
        assertCommandBehavior(
                "add Valid Name sd/01-02-2015 e/valid@endTime.butNoDatePrefix valid, address", expectedMessage);
    }

    @Test
    public void execute_add_invalidPersonData() throws Exception {
        assertCommandBehavior(
                "add []\\[;] sd/01-02-2015 e/valid@e.mail a/valid, address", Name.MESSAGE_NAME_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name sd/01-02-2015 e/valid@e.mail a/valid, address", StartTime.MESSAGE_START_TIME_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name sd/01-02-2015 e/notAnEndTime a/valid, address", EndTime.MESSAGE_ENDTIME_CONSTRAINTS);
        assertCommandBehavior(
                "add Valid Name sd/01-02-2015 e/valid@e.mail a/valid, address t/invalid_-[.tag", Tag.MESSAGE_TAG_CONSTRAINTS);

    }

    @Test
    public void execute_add_successful() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Person toBeAdded = helper.adam();
        Scheduler expectedAB = new Scheduler();
        expectedAB.addPerson(toBeAdded);

        // execute command and verify result
        assertCommandBehavior(helper.generateAddCommand(toBeAdded),
                String.format(AddCommand.MESSAGE_SUCCESS, toBeAdded),
                expectedAB,
                expectedAB.getPersonList());

    }

    @Test
    public void execute_addDuplicate_notAllowed() throws Exception {
        // setup expectations
        TestDataHelper helper = new TestDataHelper();
        Person toBeAdded = helper.adam();
        Scheduler expectedAB = new Scheduler();
        expectedAB.addPerson(toBeAdded);

        // setup starting state
        model.addPerson(toBeAdded); // person already in internal scheduler

        // execute command and verify result
        assertCommandBehavior(
                helper.generateAddCommand(toBeAdded),
                AddCommand.MESSAGE_DUPLICATE_PERSON,
                expectedAB,
                expectedAB.getPersonList());

    }


    @Test
    public void execute_list_showsAllPersons() throws Exception {
        // prepare expectations
        TestDataHelper helper = new TestDataHelper();
        Scheduler expectedAB = helper.generateScheduler(2);
        List<? extends ReadOnlyPerson> expectedList = expectedAB.getPersonList();

        // prepare scheduler state
        helper.addToModel(model, 2);

        assertCommandBehavior("list",
                ListCommand.MESSAGE_SUCCESS,
                expectedAB,
                expectedList);
    }


    /**
     * Confirms the 'invalid argument index number behaviour' for the given command
     * targeting a single person in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single person in the last shown list based on visible index.
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
     * targeting a single person in the shown list, using visible index.
     * @param commandWord to test assuming it targets a single person in the last shown list based on visible index.
     */
    private void assertIndexNotFoundBehaviorForCommand(String commandWord) throws Exception {
        String expectedMessage = MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
        TestDataHelper helper = new TestDataHelper();
        List<Person> personList = helper.generatePersonList(2);

        // set AB state to 2 persons
        model.resetData(new Scheduler());
        for (Person p : personList) {
            model.addPerson(p);
        }

        assertCommandBehavior(commandWord + " 3", expectedMessage, model.getScheduler(), personList);
    }

    @Test
    public void execute_selectInvalidArgsFormat_errorMessageShown() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE);
        assertIncorrectIndexFormatBehaviorForCommand("select", expectedMessage);
    }

    @Test
    public void execute_selectIndexNotFound_errorMessageShown() throws Exception {
        assertIndexNotFoundBehaviorForCommand("select");
    }

    @Test
    public void execute_select_jumpsToCorrectPerson() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Person> threePersons = helper.generatePersonList(3);

        Scheduler expectedAB = helper.generateScheduler(threePersons);
        helper.addToModel(model, threePersons);

        assertCommandBehavior("select 2",
                String.format(SelectCommand.MESSAGE_SELECT_PERSON_SUCCESS, 2),
                expectedAB,
                expectedAB.getPersonList());
        assertEquals(1, targetedJumpIndex);
        assertEquals(model.getFilteredPersonList().get(1), threePersons.get(1));
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

    @Test
    public void execute_delete_removesCorrectPerson() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        List<Person> threePersons = helper.generatePersonList(3);

        Scheduler expectedAB = helper.generateScheduler(threePersons);
        expectedAB.removePerson(threePersons.get(1));
        helper.addToModel(model, threePersons);

        assertCommandBehavior("delete 2",
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, threePersons.get(1)),
                expectedAB,
                expectedAB.getPersonList());
    }


    @Test
    public void execute_find_invalidArgsFormat() throws Exception {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertCommandBehavior("find ", expectedMessage);
    }

    @Test
    public void execute_find_onlyMatchesFullWordsInNames() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person pTarget1 = helper.generatePersonWithName("bla bla KEY bla");
        Person pTarget2 = helper.generatePersonWithName("bla KEY bla bceofeia");
        Person p1 = helper.generatePersonWithName("KE Y");
        Person p2 = helper.generatePersonWithName("KEYKEYKEY sduauo");

        List<Person> fourPersons = helper.generatePersonList(p1, pTarget1, p2, pTarget2);
        Scheduler expectedAB = helper.generateScheduler(fourPersons);
        List<Person> expectedList = helper.generatePersonList(pTarget1, pTarget2);
        helper.addToModel(model, fourPersons);

        assertCommandBehavior("find KEY",
                Command.getMessageForPersonListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_isNotCaseSensitive() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person p1 = helper.generatePersonWithName("bla bla KEY bla");
        Person p2 = helper.generatePersonWithName("bla KEY bla bceofeia");
        Person p3 = helper.generatePersonWithName("key key");
        Person p4 = helper.generatePersonWithName("KEy sduauo");

        List<Person> fourPersons = helper.generatePersonList(p3, p1, p4, p2);
        Scheduler expectedAB = helper.generateScheduler(fourPersons);
        List<Person> expectedList = fourPersons;
        helper.addToModel(model, fourPersons);

        assertCommandBehavior("find KEY",
                Command.getMessageForPersonListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }

    @Test
    public void execute_find_matchesIfAnyKeywordPresent() throws Exception {
        TestDataHelper helper = new TestDataHelper();
        Person pTarget1 = helper.generatePersonWithName("bla bla KEY bla");
        Person pTarget2 = helper.generatePersonWithName("bla rAnDoM bla bceofeia");
        Person pTarget3 = helper.generatePersonWithName("key key");
        Person p1 = helper.generatePersonWithName("sduauo");

        List<Person> fourPersons = helper.generatePersonList(pTarget1, p1, pTarget2, pTarget3);
        Scheduler expectedAB = helper.generateScheduler(fourPersons);
        List<Person> expectedList = helper.generatePersonList(pTarget1, pTarget2, pTarget3);
        helper.addToModel(model, fourPersons);

        assertCommandBehavior("find key rAnDoM",
                Command.getMessageForPersonListShownSummary(expectedList.size()),
                expectedAB,
                expectedList);
    }


    /**
     * A utility class to generate test data.
     */
    class TestDataHelper{

        Person adam() throws Exception {
            Name name = new Name("Adam Brown");
            StartTime privateStartTime = new StartTime("111111");
            EndTime endTime = new EndTime("adam@gmail.com");
            Date privateDate = new Date("111, alpha street");
            Tag tag1 = new Tag("tag1");
            Tag tag2 = new Tag("tag2");
            UniqueTagList tags = new UniqueTagList(tag1, tag2);
            return new Person(name, privateStartTime, endTime, privateDate, tags);
        }

        /**
         * Generates a valid person using the given seed.
         * Running this function with the same parameter values guarantees the returned person will have the same state.
         * Each unique seed will generate a unique Person object.
         *
         * @param seed used to generate the person data field values
         */
        Person generatePerson(int seed) throws Exception {
            return new Person(
                    new Name("Person " + seed),
                    new StartTime("" + Math.abs(seed)),
                    new EndTime(seed + "@endTime"),
                    new Date("House of " + seed),
                    new UniqueTagList(new Tag("tag" + Math.abs(seed)), new Tag("tag" + Math.abs(seed + 1)))
            );
        }

        /** Generates the correct add command based on the person given */
        String generateAddCommand(Person p) {
            StringBuffer cmd = new StringBuffer();

            cmd.append("add ");

            cmd.append(p.getName().toString());
            cmd.append(" p/").append(p.getStartTime());
            cmd.append(" e/").append(p.getEndTime());
            cmd.append(" a/").append(p.getDate());

            UniqueTagList tags = p.getTags();
            for(Tag t: tags){
                cmd.append(" t/").append(t.tagName);
            }

            return cmd.toString();
        }

        /**
         * Generates an Scheduler with auto-generated persons.
         */
        Scheduler generateScheduler(int numGenerated) throws Exception{
            Scheduler scheduler = new Scheduler();
            addToScheduler(scheduler, numGenerated);
            return scheduler;
        }

        /**
         * Generates an Scheduler based on the list of Persons given.
         */
        Scheduler generateScheduler(List<Person> persons) throws Exception{
            Scheduler scheduler = new Scheduler();
            addToScheduler(scheduler, persons);
            return scheduler;
        }

        /**
         * Adds auto-generated Person objects to the given Scheduler
         * @param scheduler The Scheduler to which the Persons will be added
         */
        void addToScheduler(Scheduler scheduler, int numGenerated) throws Exception{
            addToScheduler(scheduler, generatePersonList(numGenerated));
        }

        /**
         * Adds the given list of Persons to the given Scheduler
         */
        void addToScheduler(Scheduler scheduler, List<Person> personsToAdd) throws Exception{
            for(Person p: personsToAdd){
                scheduler.addPerson(p);
            }
        }

        /**
         * Adds auto-generated Person objects to the given model
         * @param model The model to which the Persons will be added
         */
        void addToModel(Model model, int numGenerated) throws Exception{
            addToModel(model, generatePersonList(numGenerated));
        }

        /**
         * Adds the given list of Persons to the given model
         */
        void addToModel(Model model, List<Person> personsToAdd) throws Exception{
            for(Person p: personsToAdd){
                model.addPerson(p);
            }
        }

        /**
         * Generates a list of Persons based on the flags.
         */
        List<Person> generatePersonList(int numGenerated) throws Exception{
            List<Person> persons = new ArrayList<>();
            for(int i = 1; i <= numGenerated; i++){
                persons.add(generatePerson(i));
            }
            return persons;
        }

        List<Person> generatePersonList(Person... persons) {
            return Arrays.asList(persons);
        }

        /**
         * Generates a Person object with given name. Other fields will have some dummy values.
         */
        Person generatePersonWithName(String name) throws Exception {
            return new Person(
                    new Name(name),
                    new StartTime("1"),
                    new EndTime("1@endTime"),
                    new Date("House of 1"),
                    new UniqueTagList(new Tag("tag"))
            );
        }
    }
}
