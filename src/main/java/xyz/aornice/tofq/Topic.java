package xyz.aornice.tofq;

/**
 * Created by robin on 10/04/2017.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.harbour.LocalHarbour;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static xyz.aornice.tofq.TopicFileFormat.Header;
import static xyz.aornice.tofq.TopicFileFormat.FileName;

public class Topic {
    private static final Logger logger = LogManager.getLogger(Topic.class);

    private String name;
    private AtomicLong maxId;
    private AtomicLong maxStoredId;
    private String newestFile;
    private long startId;
    private Harbour harbour;

    public Topic(String name, String newestFile, Harbour harbour) {
        this.name = name;
        this.newestFile = newestFile;
        setHarbour(harbour);
        loadInfo();
    }

    public Topic(String name, String newestFile) {
        this(name, newestFile, new LocalHarbour());
    }

    private void loadInfo() {
        startId = harbour.getLong(newestFile, Header.ID_START_OFFSET_BYTE);
        final int count = harbour.getInt(newestFile, Header.COUNT_OFFSET_BYTE);
        maxId = new AtomicLong(startId + count - 1);
        maxStoredId = new AtomicLong(startId + count - 1);
    }

    public String getName() {
        return name;
    }

    /**
     * thread-safe
     *
     * @return the incremented max id of cargo
     */
    public long incrementAndGetId() {
        return maxId.incrementAndGet();
    }

    public long getMaxStoredId() {
        return maxStoredId.get();
    }

    /**
     * thread-safe
     *
     * @param id - the max id of the cargo has been deposited
     * @return if return true, represent set success. It might be failed, cause of
     * current saved cargo max id is large than the id
     */
    public boolean setMaxStoredId(long id) {
        long origin;
        do {
            origin = this.maxStoredId.get();
            if (origin >= id) return false;
        } while (!this.maxStoredId.compareAndSet(origin, id));
        return true;
    }

    public String getNewestFile() {
        return newestFile;
    }

    public int getCount() {
        return (int) (maxStoredId.get() - startId) + 1;
    }

    public String newTopicFile() {
        String date = FileName.DATE_FORMAT.format(new Date());
        final String basePath = Setting.TOPIC_ROOT + File.separator + getName() + File.separator;
        int num = 0, prefixLen = basePath.length();
        if (newestFile.substring(prefixLen, prefixLen + 8).equals(date))
            num = Integer.valueOf(newestFile.substring(prefixLen + 8, newestFile.length() - 4)) + 1;
        final String newTopicFile = basePath + date + num + ".tof";

        final long startId = getMaxStoredId() + 1;
        harbour.create(newTopicFile);
        harbour.put(newTopicFile, startId, Header.ID_START_OFFSET_BYTE);
        harbour.put(newTopicFile, 0, Header.COUNT_OFFSET_BYTE);

        this.startId = startId;
        this.newestFile = newTopicFile;
        logger.info("New topic file {} with startId {}", newTopicFile, startId);
        return newTopicFile;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Topic)) return false;
        return this.name.equals(((Topic) obj).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public long getStartId() {
        return startId;
    }

    public void setHarbour(Harbour harbour) {
        this.harbour = harbour;
    }
}
