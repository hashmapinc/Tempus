package com.hashmapinc.writer;

import com.hashmapinc.offset.OffsetManager;
import com.hashmapinc.reader.Reader;
import com.hashmapinc.records.RuleLogRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public class FileSystemLogReaderWriter implements Writer<RuleLogRecord>, Reader<UUID, FileSystemLogReaderWriter.LogRecordWrapper>{

    private final ReentrantLock lock = new ReentrantLock();
    private static final String STORAGE_FILE_PREFIX = "rule-buffer-";
    private File logDir;
    private final UUID tenantId;
    private final UUID ruleId;
    private AtomicLong sequenceGenerator;
    private AtomicInteger fileCounter;
    private ConcurrentLinkedDeque<RuleLogRecord> buffer;
    private OffsetManager<UUID, Long> offsetManager;

    public FileSystemLogReaderWriter(final String logDir, final UUID tenantId, final UUID ruleId){
        this.tenantId = tenantId;
        this.ruleId = ruleId;
        this.buffer = new ConcurrentLinkedDeque<>();
        initFileSystem(logDir);
    }

    //Should we Init buffer as well with the data already in file system ?
    private void initFileSystem(String logDir){
        this.logDir = new File(logDir + File.pathSeparator + tenantId.toString() + File.pathSeparator + ruleId.toString());
        if(!this.logDir.exists() && !this.logDir.mkdirs()){
            log.error("Error while creating logs directory for tenant {}", this.tenantId);
        }
    }

    @Override
    public void append(RuleLogRecord record) {
        long msgId = sequenceGenerator.incrementAndGet();
        RuleLogRecord updated = new RuleLogRecord(msgId, record);
        //Configure buffer capacity
        if(buffer.size() >= 2000){
            flushBufferToFileSystem();
        }
        buffer.add(updated);
    }

    private void flushBufferToFileSystem() {
        FileOutputStream bStream = null;
        ObjectOutputStream stream = null;
        try {
            bStream = new FileOutputStream(new File(logDir, STORAGE_FILE_PREFIX + fileCounter.incrementAndGet()));
            stream = new ObjectOutputStream(bStream);
            for(RuleLogRecord r: buffer){
                stream.writeObject(r);
            }
            buffer.clear();
        } catch (Exception e) {
            //log error
        }finally {
            if (bStream != null) {
                try {
                    bStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public LogRecordWrapper next(UUID key) {
        RuleLogRecord record = null;
        if(key.equals(ruleId)){
            if(buffer.peekFirst().getLogId() > offsetManager.current(key)){
                long fileCounter = offsetManager.current(key) / 2000;
                File store = new File(logDir, STORAGE_FILE_PREFIX + fileCounter);
                if(fileCounter > 0 && store.exists()) {
                    try {
                        FileInputStream stream = new FileInputStream(store);
                        ObjectInputStream inputStream = new ObjectInputStream(stream);

                        while (true){

                        }


                    }catch (Exception e){

                    }
                }

                //Read from file
            }else{
                record = buffer.getFirst();
            }
        }
        return new LogRecordWrapper(record, r -> offsetManager.commit(r.getMsgId(), key), error -> log.error(error.getMessage()) );
    }

    @AllArgsConstructor
    public static class LogRecordWrapper{
        private final RuleLogRecord record;
        private final Consumer<RuleLogRecord> successCallback;
        private final Consumer<Throwable> errorCallback;
    }
}
