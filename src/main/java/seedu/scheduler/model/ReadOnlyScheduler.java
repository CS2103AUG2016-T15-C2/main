package seedu.scheduler.model;


import seedu.scheduler.model.entry.ReadOnlyEntry;
import seedu.scheduler.model.entry.UniqueEntryList;
import seedu.scheduler.model.tag.Tag;
import seedu.scheduler.model.tag.UniqueTagList;

import java.util.List;

/**
 * Unmodifiable view of an scheduler
 */
public interface ReadOnlyScheduler {

    UniqueTagList getUniqueTagList();

    UniqueEntryList getUniqueEntryList();

    /**
     * Returns an unmodifiable view of entrys list
     */
    List<ReadOnlyEntry> getEntryList();

    /**
     * Returns an unmodifiable view of tags list
     */
    List<Tag> getTagList();

}
