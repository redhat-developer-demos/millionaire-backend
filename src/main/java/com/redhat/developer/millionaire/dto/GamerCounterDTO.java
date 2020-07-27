package com.redhat.developer.millionaire.dto;

public class GamerCounterDTO implements ServerSideEventMessage {
    
    private long numberOfGamers;

    public GamerCounterDTO(long numberOfGamers) {
        this.numberOfGamers = numberOfGamers;
    }
    
    public long getNumberOfGamers() {
        return numberOfGamers;
    }
    
}