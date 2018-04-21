package com.hashmapinc.server.actors.shared.application;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import com.hashmapinc.server.actors.ActorSystemContext;
import com.hashmapinc.server.actors.application.ApplicationActor;
import com.hashmapinc.server.common.data.id.TenantId;
import lombok.extern.slf4j.Slf4j;
import akka.actor.Props;

@Slf4j
public abstract class ApplicationManager {
    protected final ActorSystemContext systemContext;

    public ApplicationManager(ActorSystemContext systemContext) {
        this.systemContext = systemContext;
    }

    abstract TenantId getTenantId();

    public ActorRef getOrCreateApplicationActor(ActorContext context){
        return context.actorOf(Props.create(new ApplicationActor.ActorCreator(systemContext, getTenantId())));
    }
}
