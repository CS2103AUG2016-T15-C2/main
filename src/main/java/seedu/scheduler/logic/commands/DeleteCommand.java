package seedu.scheduler.logic.commands;

import seedu.scheduler.commons.core.Messages;
import seedu.scheduler.commons.core.UnmodifiableObservableList;
import seedu.scheduler.model.entry.*;
import seedu.scheduler.model.entry.UniqueEntryList.DuplicateEntryException;
import seedu.scheduler.model.entry.UniqueEntryList.EntryNotFoundException;

/**
 * Deletes a entry identified using it's last displayed index from the
 * scheduler.
 */
public class DeleteCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "delete";
    public static final String COMMAND_WORD2 = "d";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the entry identified by the index number used in the last entry listing.\n"
            + "Parameters: INDEX (must be a positive integer)\n" + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_ENTRY_SUCCESS = "Deleted Entry: %1$s";

    private final int targetIndex;
    //@@author A0152962B
    private int indexInScheduler;
    private Entry prevEntry;
    //@@author 

    public DeleteCommand(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute() {

        UnmodifiableObservableList<ReadOnlyEntry> lastShownList = model.getFilteredEntryList();

        if (lastShownList.size() < targetIndex) {
            indicateAttemptToExecuteIncorrectCommand();
            return new CommandResult(Messages.MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX);
        }

        ReadOnlyEntry entryToDelete = lastShownList.get(targetIndex - 1);
        indexInScheduler = model.getScheduler().getEntryList().indexOf(entryToDelete) + 1;
        try {
            model.deleteEntry(entryToDelete);
        } catch (EntryNotFoundException pnfe) {
            assert false : "The target entry cannot be missing";
        }
        //@@author A0152962B
        prevEntry = (Entry) entryToDelete;
        undoManager.stackCommand(this);
        //@@author
        return new CommandResult(String.format(MESSAGE_DELETE_ENTRY_SUCCESS, entryToDelete));
    }

    //@@author A0152962B
    @Override
    public void undo() {
        try {
            model.addEntryAtIndex(indexInScheduler, prevEntry);
        } catch (DuplicateEntryException e) { }
    }

    @Override
    public void redo() {
        try {
            model.deleteEntry(prevEntry);
        } catch (EntryNotFoundException e) { }
    }
    //@@author

}
