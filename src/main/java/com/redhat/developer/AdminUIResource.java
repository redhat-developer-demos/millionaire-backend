package com.redhat.developer;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.redhat.developer.millionaire.model.Contest;
import com.redhat.developer.millionaire.model.Question;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/admin-ui")
public class AdminUIResource {
    
    @Inject
    Template admin;

    @Inject
    Template contestCrud;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance render(){
        return admin.data("username", securityIdentity.getPrincipal().getName());
    }

    @GET
    @Path("/crud/{contestId}")
    @Produces(MediaType.TEXT_HTML) 
    public TemplateInstance renderContestTable(@PathParam("contestId") String contestId) {
        List<Question> questions = Question.listAll();
        return contestCrud.data("questions",questions);
    } 
    
}