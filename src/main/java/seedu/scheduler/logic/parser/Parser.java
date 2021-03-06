package seedu.scheduler.logic.parser;

import seedu.scheduler.logic.commands.*;
import seedu.scheduler.commons.util.StringUtil;
import seedu.scheduler.commons.exceptions.IllegalValueException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seedu.scheduler.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.scheduler.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

/**
 * Parses user input.
 */
public class Parser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    private static final Pattern ENTRY_INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    private static final Pattern KEYWORDS_ARGS_FORMAT =
            Pattern.compile("(?<keywords>\\S+(?:\\s+\\S+)*)"); // one or more keywords separated by whitespace
    
    //@@author A0139956L
    private static final Pattern PATH_DATA_ARGS_FORMAT =
            Pattern.compile("(?<name>[\\p{Alnum}|/]+)"); //data/ <---
   
    //@@author A0161210A
    // Pattern allows all parameters to be optional and allow natural language inputs
    private static final Pattern ENTRY_DATA_ARGS_FORMAT = // '/' forward slashes are reserved for delimiter prefixes
            Pattern.compile("(?<name>[^/]+)?"
                    + "(?<isStartTimePrivate>p?)(?:(from/|f/|st/)(?<startTime>[^/]+))?"
                    + "(?<isEndTimePrivate>p?)(?:(to/|et/)(?<endTime>[^/]+))?"
                    + "(?<isDatePrivate>p?)(?:(on/|sdate/|sd/|)(?<date>[^/]+))?"
                    + "(?<isEndDatePrivate>p?)(?:(ed/|by/|edate/)(?<endDate>[^/]+))?"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags
    //@@author A0152962B
    private static final Pattern ENTRY_EDIT_ARGS_FORMAT = 
            Pattern.compile("(?<targetIndex>\\d+)"
                    + "(?<name>[^/]+)?"
                    + "(?<isStartTimePrivate>p?)(?:(from/|f/|st/)(?<startTime>[^/]+))?"
                    + "(?<isEndTimePrivate>p?)(?:(to/|et/)(?<endTime>[^/]+))?"
                    + "(?<isDatePrivate>p?)(?:(on/|date/|sd/|by/)(?<date>[^/]+))?"
                    + "(?<isEndDatePrivate>p?)(?:(edate/|ed/)(?<endDate>[^/]+))?"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags
    //@@author A0152962B-unused
    //Unused: update feature removed, no longer require the regular expression
    /*private static final Pattern ENTRY_UPDATE_ARGS_FORMAT = 
            Pattern.compile("(?<targetIndex>\\d+) "
                    + "(?<isStartTimePrivate>p?)(?:(from/|f/|st/)(?<startTime>[^/]+))?"
                    + "(?<isEndTimePrivate>p?)(?:(to/|et/)(?<endTime>[^/]+))?"
                    + "(?<isDatePrivate>p?)(?:(on/|date/|sd/|by/)(?<date>[^/]+))?"
                    + "(?<isEndDatePrivate>p?)(?:(edate/|ed/)(?<endDate>[^/]+))?"
                    + "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags
    */
    //@@author

    public Parser() {
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput
     *            full user input string
     * @return the command based on the user input
     */
    public Command parseCommand(String userInput) {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");
      //@@author A0161210A
        switch (commandWord) {

        case AddCommand.COMMAND_WORD:
            return prepareAdd(arguments);
        
        case AddCommand.COMMAND_WORD2:
            return prepareAdd(arguments);
        //@@author

        case DeleteCommand.COMMAND_WORD:
            return prepareDelete(arguments);

        case DeleteCommand.COMMAND_WORD2:
            return prepareDelete(arguments);

        //@@author A0152962B
        case EditCommand.COMMAND_WORD:
            return prepareEdit(arguments);
            
        case EditCommand.COMMAND_WORD2:
            return prepareEdit(arguments);
        //@@author A0152962B-unused
        //Unused: update feature removed, removed to prevent registering an unimplemented feature
        /*case UpdateCommand.COMMAND_WORD:
            return prepareUpdate(arguments);
            
        case UpdateCommand.COMMAND_WORD2:
            return prepareUpdate(arguments);
        */
        //@@author    
        case MarkedCommand.COMMAND_WORD:
            return prepareMarked(arguments);
            
        case MarkedCommand.COMMAND_WORD2:
            return prepareMarked(arguments);
            
        case ClearCommand.COMMAND_WORD:
            return new ClearCommand();
            
        case ClearCommand.COMMAND_WORD2:
            return new ClearCommand();

        case FindCommand.COMMAND_WORD:
            return prepareFind(arguments);
            
        case FindCommand.COMMAND_WORD2:
            return prepareFind(arguments);

        case ListCommand.COMMAND_WORD:
            //return new ListCommand();
            //@@author A0139956L-unused
        	return prepareList(arguments);
            //@@author A0139956L
        case ListCommand.COMMAND_WORD2:
        	//return new ListCommand();
        	//@@author A0139956L-unused
            return prepareList(arguments);

        //@@author A0139956L    
        case PathCommand.COMMAND_WORD:
            return preparePath(arguments);
            
        case PathCommand.COMMAND_WORD2:
            return preparePath(arguments);
        //@@author  
        
        case ExitCommand.COMMAND_WORD:
            return new ExitCommand();
           
        case ExitCommand.COMMAND_WORD2:
            return new ExitCommand();
          
        //@@author A0152962B
        case UndoableCommand.COMMAND_WORD_UNDO:
            UndoableCommand.undoManager.undo();
            return null;

        case UndoableCommand.COMMAND_WORD_REDO:
            UndoableCommand.undoManager.redo();
            return null;
        //@@author
            
        case HelpCommand.COMMAND_WORD:
            return new HelpCommand();

        case HelpCommand.COMMAND_WORD2:
            return new HelpCommand();

        default:
            return new IncorrectCommand(MESSAGE_UNKNOWN_COMMAND);
        }
    }

    /**
     * Parses arguments in the context of the add entry command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareAdd(String args) {
        final Matcher matcher = ENTRY_DATA_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        try {
            return new AddCommand(matcher.group("name"), matcher.group("startTime"), matcher.group("endTime"),
                    matcher.group("date"), matcher.group("endDate"), getTagsFromArgs(matcher.group("tagArguments")));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

    /**
     * Extracts the new entry's tags from the add command's tag arguments
     * string. Merges duplicate tag strings.
     */
    private static Set<String> getTagsFromArgs(String tagArguments) throws IllegalValueException {
        // no tags
        if (tagArguments.isEmpty()) {
            return Collections.emptySet();
        }
        // replace first delimiter prefix, then split
        final Collection<String> tagStrings = Arrays.asList(tagArguments.replaceFirst(" t/", "").split(" t/"));
        return new HashSet<>(tagStrings);
    }

    /**
     * Parses arguments in the context of the delete entry command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareDelete(String args) {

        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        return new DeleteCommand(index.get());
    }

    //@@author A0152962B
    /**
     * Parses arguments into the context of the edit entry command.
     * 
     * @param args full command args string
     * @return the newly prepared command
     */
    private Command prepareEdit(String args) {
        final Matcher matcher = ENTRY_EDIT_ARGS_FORMAT.matcher(args.trim());
        // Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        try {
            return new EditCommand(Integer.parseInt(matcher.group("targetIndex")), matcher.group("name"),
                    matcher.group("startTime"), matcher.group("endTime"), matcher.group("date"), matcher.group("endDate"),
                    getTagsFromArgs(matcher.group("tagArguments")));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }

    }
    
    //@@author A0152962B-unused
    /**
     * Parses arguments into the context of the update entry command.
     * 
     * @param args full command args string
     * @return the newly prepared command
     */
    //Unused: update feature removed, no longer needed
    /*private Command prepareUpdate(String args) {
        final Matcher matcher = ENTRY_UPDATE_ARGS_FORMAT.matcher(args.trim());
        if(!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UpdateCommand.MESSAGE_USAGE));
        }
        
        try {
            return new UpdateCommand(Integer.parseInt(matcher.group("targetIndex")),
                    matcher.group("startTime"), matcher.group("endTime"), matcher.group("date"), matcher.group("endDate"),
                    getTagsFromArgs(matcher.group("tagArguments")));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }
    */
    
    //@@author A0126090N
    /**
     * Parses arguments in the context of the completed entry command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command prepareMarked(String args) {
        Optional<Integer> index = parseIndex(args);
        // Validate arg string format
        if(!index.isPresent()){
            return new IncorrectCommand(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkedCommand.MESSAGE_USAGE));    
        }
        
        return new MarkedCommand(index.get());
        
    }

    /**
     * Returns the specified index in the {@code command} IF a positive unsigned
     * integer is given as the index. Returns an {@code Optional.empty()}
     * otherwise.
     */
    private Optional<Integer> parseIndex(String command) {
        final Matcher matcher = ENTRY_INDEX_ARGS_FORMAT.matcher(command.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String index = matcher.group("targetIndex");
        if (!StringUtil.isUnsignedInteger(index)) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(index));

    }

    /**
     * Parses arguments in the context of the find entry command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareFind(String args) {
        final Matcher matcher = KEYWORDS_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        // keywords delimited by whitespace
        final String[] keywords = matcher.group("keywords").split("\\s+");
        final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
        return new FindCommand(keywordSet);
    }
    //@@author A0139956L-unused
    //unused: Not enough time to do DateQualifier  
/*    private Command prepareList(String args) {
    	if(args.contains("sort") || args.equals("")) {
    		return new ListCommand(args);
    	}
    	else {
    		return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
    	}
    }
*/    
    private Command prepareList(String args) {
    	if(args.equals(""))
    		return new ListCommand();
    	else {
    		return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
    	}    		
    }
    
    /**
     * Parses arguments in the context of the file path command.
     *
     * @param args full command args string
     * @return the prepared command
     */
    private Command preparePath(String args) {
        final Matcher matcher = PATH_DATA_ARGS_FORMAT.matcher(args.trim());
        //Validate arg string format
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    PathCommand.MESSAGE_USAGE));
        }
        else {
            String filePath = matcher.group("name").trim().replaceAll("/$", "") + ".xml";                   //store input to filePath
            return new PathCommand(filePath);       //push input to PathCommand
        }
    }
    //@@author
}
