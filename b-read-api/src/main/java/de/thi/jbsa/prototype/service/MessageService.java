package de.thi.jbsa.prototype.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.jms.Topic;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import de.thi.jbsa.prototype.domain.MessageDoc;
import de.thi.jbsa.prototype.model.event.AbstractEvent;
import de.thi.jbsa.prototype.model.event.Event;
import de.thi.jbsa.prototype.model.event.MentionEvent;
import de.thi.jbsa.prototype.model.event.MessagePostedEvent;
import de.thi.jbsa.prototype.model.model.Message;
import de.thi.jbsa.prototype.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageService {

  // TODO Workaround. This does not scale. Create another instance of this service and clients will have different Events
  private final List<AbstractEvent> events = new ArrayList<>(); // workaround

  private final JmsTemplate jmsTemplate;

  private final MessageRepository messageRepository;

  private final Topic topic;

  public MessageService(MessageRepository messageRepository, JmsTemplate jmsTemplate, Topic topic) {
    this.messageRepository = messageRepository;
    this.jmsTemplate = jmsTemplate;
    this.topic = topic;
  }

  private Message createMsg(MessagePostedEvent event) {
    Message msg = new Message();
    msg.setCmdUuid(event.getCmdUuid());
    msg.setContent(event.getContent());
    msg.setCreated(new Date());
    msg.setEntityId(event.getEntityId());
    msg.setEventUuid(event.getUuid());
    msg.setSenderUserId(event.getUserId());
    return msg;
  }

  public List<MessageDoc> getAllMessages() {
    return messageRepository.findAll();
  }

  public List<AbstractEvent> getEvents(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<UUID> lastEvent) {
    int indexOfLastEvent = 0;
    if (lastEvent.isPresent()) {
      indexOfLastEvent = events.stream()
                               .map(Event::getUuid)
                               .collect(Collectors.toList())
                               .indexOf(lastEvent.get());
    }
    return events
      .stream()
      .skip(indexOfLastEvent == 0 ? 0 : indexOfLastEvent + 1)
      .collect(Collectors.toList());
  }

  public void handleMentionEvent(MentionEvent event) {
    // This is a temporary event. We don't need that in the read-db.
    events.add(event);
    jmsTemplate.convertAndSend(topic, event);
  }

  public void handleMessagePostedEvent(MessagePostedEvent event) {
    MessageDoc doc = new MessageDoc(createMsg(event));
    messageRepository.save(doc);
    events.add(event);
    jmsTemplate.convertAndSend(topic, event);
  }
}
