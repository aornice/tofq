package xyz.aornice.tofq.impl;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.CargoExtraction;
import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.harbour.Harbour;
import xyz.aornice.tofq.utils.FileLocator;
import xyz.aornice.tofq.utils.TopicCenter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by drfish on 09/04/2017.
 */
public class LocalExtraction implements CargoExtraction {
    private Harbour harbour;

    public LocalExtraction(Harbour harbour) {
        this.harbour = harbour;
    }

    @Override
    public Cargo[] readAll() {
        return null;
    }

    @Override
    public Cargo read(Topic topic, long id) {
        String fileName = FileLocator.fileName(topic.getName(), id);
        // the id or topic does not exists
        if (fileName == null){
            return null;
        }
        long offset = FileLocator.messageOffset(id);

        byte[] message = harbour.get(fileName, offset);

        return new Cargo(topic, id, message);
    }

    @Override
    public Cargo[] read(Topic topic, long from, long to) {
        if (! TopicCenter.existsTopic(topic.getName())){
            return new Cargo[0];
        }

        // get the actual bound,
        long bound = Long.min(topic.getMaxStoredId()+1, to);

        Cargo[] cargos = new Cargo[(int)(bound-from)];

        long current = from;
        long nextBound = current;
        int pos = 0;

        do {
            String fileName = FileLocator.fileName(topic.getName(), current);
            // the id is out of range
            if (fileName == null) {
                break;
            }
            nextBound = FileLocator.nextBound(current);
            List<byte[]> messages;
            if (nextBound > bound){
                messages = harbour.get(fileName, current, bound);
            }else{
                messages = harbour.get(fileName, current, nextBound-1);
            }
            for (byte[] msg: messages){
                cargos[pos++] = new Cargo(topic, from+pos, msg);
            }
            current = nextBound;
        }while(nextBound < bound);

        return cargos;
    }

}
