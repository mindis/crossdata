package com.stratio.sdh.meta.statements;

import com.stratio.sdh.meta.structures.Path;

public class DropKeyspaceStatement extends Statement {
    
    private String keyspaceName;
    private boolean ifExists;  

    public DropKeyspaceStatement(String keyspaceName, boolean ifExists) {
        this.keyspaceName = keyspaceName;
        this.ifExists = ifExists;
    }    
    
    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Dropping keyspace ");        
        sb.append(keyspaceName);  
        if(ifExists){
           sb.append(" (if exists) ");
        }        
        return sb.toString();
    }

    @Override
    public Path estimatePath() {
        return Path.CASSANDRA;
    }
    
}