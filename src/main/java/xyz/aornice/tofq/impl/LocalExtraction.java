package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.utils.FileLocater;
import xyz.aornice.tofq.utils.TopicCenter;
import xyz.aornice.tofq.utils.impl.LocalFileLocator;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.util.List;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalExtraction implements CargoExtraction {
    private Harbour harbour;

    private FileLocater fileLocater = LocalFileLocator.newInstance();
    private TopicCenter topicCenter = LocalTopicCenter.newInstance();

    public LocalExtraction(Harbour harbour) {
        this.harbour = harbour;
    }

    @Override
    public Cargo[] readAll() {
        return null;
    }

    @Override
    public Cargo read(Topic topic, long id) {
        String fileName = fileLocater.fileName(topic.getName(), id);
        // the id or topic does not exists
        if (fileName == null){
            return null;
        }
        long offset = fileLocater.messageOffset(id);

        byte[] message = harbour.get(fileName, offset);

        return new Cargo(topic, id, message);
    }

    @Override
    public Cargo[] read(Topic topic, long from, long to) {
        String topicName = topic.getName();
        if (! topicCenter.existsTopic(topicName)){
            return new Cargo[0];
        }

        // get the actual bound,
        long bound = Long.min(topic.getMaxStoredId()+1, to);

        Cargo[] cargos = new Cargo[(int)(bound-from)];

        long current = from;
        long nextBound;
        int pos = 0;

        do {
            String fileName = fileLocater.fileName(topicName, current);
            // the id is out of range
            if (fileName == null) {
                break;
            }
            nextBound = fileLocater.nextBound(current);
            List<byte[]> messages;
            if (nextBound > bound){
                messages = harbour.get(fileLocater.filePath(topicName, fileName), current, bound);
            }else{
                messages = harbour.get(fileLocater.filePath(topicName, fileName), current, nextBound-1);
            }
            for (byte[] msg: messages){
                cargos[pos++] = new Cargo(topic, from+pos, msg);
            }
            current = nextBound;
        }while(nextBound < bound);

        return cargos;
    }

}
