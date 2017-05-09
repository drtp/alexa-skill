package de.rainu.alexa.config;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import de.rainu.alexa.annotation.OnIntent;
import de.rainu.alexa.annotation.OnSessionEnded;
import de.rainu.alexa.annotation.OnSessionStarted;
import de.rainu.alexa.util.Argument;
import de.rainu.alexa.util.ReflectionUtils;

public class SpeechletServletDispatcher extends SpeechletServlet implements SpeechletV2 {

  final SpeechletHandlerHolder handler;

  public SpeechletServletDispatcher(SpeechletHandlerHolder handler) {
    this.handler = handler;

    this.setSpeechlet(this);
  }

  @Override
  public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> req) {
    return (SpeechletResponse) call(handler.onLaunch, req);
  }

  @Override
  public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> req) {
    for(SpeechletHandler<OnSessionStarted> startHandler : handler.onSessionStarted) {
      call(startHandler, req);
    }
  }

  @Override
  public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> req) {
    for(SpeechletHandler<OnSessionEnded> endHandler : handler.onSessionEnded) {
      call(endHandler, req);
    }
  }

  @Override
  public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> req) {
    SpeechletHandler<OnIntent> intentHandler = handler.onIntent.get(req.getRequest().getIntent().getName());

    return (SpeechletResponse) call(intentHandler, req);
  }

  private Object call(SpeechletHandler<?> speechletHandler, SpeechletRequestEnvelope req) {
    return ReflectionUtils.call(speechletHandler.method, speechletHandler.handler,
        new Argument(SpeechletRequestEnvelope.class, req),
        new Argument(Session.class, req.getSession()),
        new Argument(Context.class, req.getContext()),
        new Argument(req.getRequest().getClass(), req.getRequest()));
  }
}
