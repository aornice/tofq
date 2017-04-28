package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.TopicFileFormat;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.utils.CargoIterator;
import xyz.aornice.tofq.harbour.LocalHarbour;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalExtractionHelper;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalExtraction implements CargoExtraction {
    private Harbour harbour;

    private TopicCenter topicCenter = LocalTopicCenter.getInstance();
    private ExtractionHelper extractionHelper = LocalExtractionHelper.getInstance();

    public LocalExtraction() {
        this(new LocalHarbour());
    }

    public LocalExtraction(Harbour harbour) {
        this.harbour = harbour;
    }

    @Override
    public CargoIterator readAll(Topic topic) {
        String fileName = topicCenter.topicOldestFile(topic.getName());
        long firstInd = extractionHelper.startIndex(topic.getName(), fileName);
        long endInd = topic.getMaxStoredId()+1;
        return new CargoIteratorImpl(topic, firstInd, endInd);
    }

    @Override
    public Cargo read(Topic topic, long id) {
        String fileName = extractionHelper.fileName(topic.getName(), id);
        // the id or topic does not exists
        if (fileName == null) {
            return null;
        }

        int msgOffset = extractionHelper.messageOffset(id);
        // TODO should use cache later
        List<Long> offsets = extractionHelper.msgByteOffsets(topic.getName(), fileName);
        if (offsets.size() == 0) {
            return null;
        }
        long byteOffsetTo = offsets.get(msgOffset);
        long byteOffsetFrom = TopicFileFormat.Header.SIZE_BYTE + TopicFileFormat.Offset.SIZE_BYTE;

        if (msgOffset != 0) {
            byteOffsetFrom = offsets.get(msgOffset - 1);
        }
        byte[] message = harbour.get(fileName, byteOffsetFrom, byteOffsetTo);

        return new Cargo(topic, id, message);
    }

    @Override
    public Cargo[] recentNCargos(Topic topic, int nCargos) {
        long endInd = topic.getMaxStoredId() + 1;
        long startInd = endInd - nCargos;

        List<byte[]> msgs = extractionHelper.read(topic.getName(), startInd, endInd);

        Cargo[] cargos = new Cargo[msgs.size()];

        for (int i = 0; i < msgs.size(); i++) {
            cargos[i] = new Cargo(topic, startInd + i, msgs.get(i));
        }

        return cargos;
    }

    @Override
    public CargoIterator recentNCargosIterator(Topic topic, long nCargos) {
        long endInd = topic.getMaxStoredId() + 1;
        long startInd = endInd - nCargos;

        return new CargoIteratorImpl(topic, startInd, endInd);
    }

    @Override
    public CargoIterator recentNDayCargos(Topic topic, int nDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date to = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -nDays);
        Date from = calendar.getTime();

        List<String> files = topicCenter.dateRangedFiles(topic.getName(), from, to);

        long startInd = extractionHelper.startIndex(topic.getName(), files.get(0));
        long endInd = topic.getMaxStoredId()+1;

        return new CargoIteratorImpl(topic, startInd, endInd);
    }

//    @Override
//    public Cargo[] recentNDayCargos(Topic topic, int nDays) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_MONTH, 1);
//        Date to = calendar.getTime();
//        calendar.add(Calendar.DAY_OF_MONTH, -nDays);
//        Date from = calendar.getTime();
//
//        List<String> files = topicCenter.dateRangedFiles(topic.getName(), from, to);
//
//        List<byte[]> msgs = extractionHelper.readInRange(topic.getName(), files.get(0), files.get(files.size() - 1), files.size());
//
//        long startInd = extractionHelper.startIndex(topic.getName(), files.get(0));
//
//        Cargo[] cargos = new Cargo[msgs.size()];
//        for (int i = 0; i < msgs.size(); i++) {
//            cargos[i] = new Cargo(topic, startInd++, msgs.get(i));
//        }
//
//        return cargos;
//    }

    @Override
    public Cargo[] read(Topic topic, long from, long to) {
        String topicName = topic.getName();
        if (!topicCenter.existsTopic(topicName)) {
            return new Cargo[0];
        }

        long bound = Long.min(topic.getMaxStoredId() + 1, to);

        // the from index is not smaller than bound
        if (bound <= from) {
            return new Cargo[0];
        }

        Cargo[] cargos = new Cargo[(int) (bound - from)];

        List<byte[]> messages = extractionHelper.read(topicName, from, bound);

        int pos = 0;

        for (byte[] msg : messages) {
            cargos[pos++] = new Cargo(topic, from + pos, msg);
        }

        return cargos;
    }

    @Override
    public CargoIterator readIterator(Topic topic, long from, long to) {
        return null;
    }


    class CargoIteratorImpl implements CargoIterator {

        private Topic topic;
        private long fromId;
        private long toId;

        private long curId;
        private long nextBound;

        private Cargo[] tmpCargos;
        private int curListInd;

        public CargoIteratorImpl(Topic topic, long fromId, long toId) {
            this.topic = topic;
            this.fromId = fromId;
            this.toId = toId;
            this.curId = fromId;
            this.nextBound = fromId;

            // nextBound is the smaller of these two
            //long nextStartId = extractionHelper.nextStartIndex(fromId);
            //this.nextBound = toId > nextStartId ? nextStartId : toId ;
        }

        @Override
        public boolean hasNext() {
            return curId < toId;
        }

        @Override
        public Cargo next() {
            if (curId >= toId) {
                throw new NoSuchElementException();
            }
            if (tmpCargos == null || curId >= nextBound) {
                long nextStartId = extractionHelper.nextStartIndex(curId);
                this.nextBound = toId > nextStartId ? nextStartId : toId ;

                tmpCargos = read(topic, curId, nextBound);
                curListInd = 0;
            }

            curId ++;

            return tmpCargos[curListInd++];

        }
    }



}
