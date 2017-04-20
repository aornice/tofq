package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalExtractionHelper;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalExtraction implements CargoExtraction {
    private Harbour harbour;

    private TopicCenter topicCenter = LocalTopicCenter.newInstance();
    private ExtractionHelper extractionHelper = LocalExtractionHelper.newInstance();

    public LocalExtraction(Harbour harbour) {
        this.harbour = harbour;
    }

    @Override
    public Cargo[] readAll() {
        return null;
    }

    @Override
    public Cargo read(Topic topic, long id) {
        String fileName = extractionHelper.fileName(topic.getName(), id);
        // the id or topic does not exists
        if (fileName == null){
            return null;
        }

        int msgOffset = extractionHelper.messageOffset(id);
        // TODO should use cache later
        long[] offsets = extractionHelper.msgByteOffsets(topic.getName(), fileName);

        long byteOffsetTo = offsets[msgOffset];
        long byteOffsetFrom = 0;

        if (msgOffset != 0){
            byteOffsetFrom = offsets[msgOffset-1];
        }
        byte[] message = harbour.get(fileName, byteOffsetFrom, byteOffsetTo);

        return new Cargo(topic, id, message);
    }

    @Override
    public Cargo[] recentNCargos(Topic topic, int nCargos) {
        long endInd = topic.getMaxStoredId()+1;
        long startInd = endInd-nCargos;

        List<byte[]> msgs = extractionHelper.read(topic.getName(), startInd, endInd);

        Cargo[] cargos = new Cargo[msgs.size()];

        for (int i=0;i<msgs.size(); i++){
            cargos[i] = new Cargo(topic, startInd+i, msgs.get(i));
        }

        return cargos;
    }

    @Override
    public Cargo[] recentNDayCargos(Topic topic, int nDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date to = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -nDays);
        Date from = calendar.getTime();

        List<String> files = topicCenter.dateRangedFiles(topic.getName(), from, to);

        List<byte[]> msgs = extractionHelper.readInRange(topic.getName(), files.get(0), files.get(files.size()-1), files.size());

        long startInd = extractionHelper.startIndex(topic.getName(), files.get(0));

        Cargo[] cargos = new Cargo[msgs.size()];
        for (int i=0;i<msgs.size(); i++){
            cargos[i] = new Cargo(topic, startInd++, msgs.get(i));
        }

        return cargos;
    }

    @Override
    public Cargo[] read(Topic topic, long from, long to) {
        String topicName = topic.getName();
        if (! topicCenter.existsTopic(topicName)){
            return new Cargo[0];
        }

        long bound = Long.min(topic.getMaxStoredId()+1, to);

        // the from index is not smaller than bound
        if (bound <= from){
            return new Cargo[0];
        }

        Cargo[] cargos = new Cargo[(int)(bound-from)];

        List<byte[]> messages = extractionHelper.read(topicName, from, bound);

        int pos = 0;

        for (byte[] msg: messages){
            cargos[pos++] = new Cargo(topic, from+pos, msg);
        }

        return cargos;
    }

}
