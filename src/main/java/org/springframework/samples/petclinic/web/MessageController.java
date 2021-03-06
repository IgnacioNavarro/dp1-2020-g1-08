package org.springframework.samples.petclinic.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Attachment;
import org.springframework.samples.petclinic.model.Message;
import org.springframework.samples.petclinic.model.Tag;
import org.springframework.samples.petclinic.model.ToDo;
import org.springframework.samples.petclinic.model.UserTW;
import org.springframework.samples.petclinic.service.AttachmentService;
import org.springframework.samples.petclinic.service.MessageService;
import org.springframework.samples.petclinic.service.TagService;
import org.springframework.samples.petclinic.service.ToDoService;
import org.springframework.samples.petclinic.service.UserTWService;
import org.springframework.samples.petclinic.validation.TagLimitProjectException;
import org.springframework.samples.petclinic.validation.ToDoMaxToDosPerAssigneeException;
import org.springframework.samples.petclinic.validation.TooBigFileException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
public class MessageController {

    private final MessageService messageService;
    private final UserTWService userService;
    private final TagService tagService;
    private final ToDoService toDoService;
    private final AttachmentService attachmentService;

    @Autowired
    public MessageController(MessageService messageService, UserTWService userService, TagService tagService,
            ToDoService toDoService, AttachmentService attachmentService) {
        this.messageService = messageService;
        this.userService = userService;
        this.tagService = tagService;
        this.toDoService = toDoService;
        this.attachmentService = attachmentService;

    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping(value = "/api/message/inbox")
    public List<Message> getMyInboxMessages(HttpServletRequest r) {
        try {
            log.info("Getting inbox messages");
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            UserTW user = userService.findUserById(userId);
            List<Message> messageList = (messageService.findMessagesByUserId(user)).stream()
                    .collect(Collectors.toList());
            log.info("Succeed");
            Collections.reverse(messageList);
            return messageList;
        } catch (DataAccessException d) {
            log.error("ERROR: " + d.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't get inbox");
        }
    }

    @GetMapping(value = "/api/message/sent")
    public List<Message> getMySentMessages(HttpServletRequest r) {
        try {
            log.info("Getting sent messages");
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            List<Message> messageList = (messageService.findMessagesSentByUserId(userId)).stream()
                    .collect(Collectors.toList());
            log.info("Succeed");
            Collections.reverse(messageList);
            return messageList;
        } catch (DataAccessException d) {
            log.error("ERROR: " + d.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find messages");
        }
    }

    @GetMapping(value = "/api/message/byTag")
    public List<Message> getMessagesByTag(HttpServletRequest r, @RequestParam(required = true) int tagId) {
        try {
            log.info("Getting messages by tag with id: " + tagId);
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            Tag tag = tagService.findTagById(tagId);
            UserTW user = userService.findUserById(userId);
            List<Message> messageList = (messageService.findMessagesByTag(user, tag)).stream()
                    .collect(Collectors.toList());
            log.info("Succeed");
            Collections.reverse(messageList);
            return messageList;
        } catch (DataAccessException d) {
            log.error("ERROR: " + d.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find messages");
        }
    }

    @GetMapping(value = "/api/message/bySearch")
    public List<Message> getMessagesBySearch(HttpServletRequest r, @RequestParam(required = true) String search) {
        try {
            log.info("Searching for messages, keywords: " + search);
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            UserTW user = userService.findUserById(userId);
            List<Message> messageList = (messageService.findMessagesBySearch(user, search.toLowerCase())).stream()
                    .collect(Collectors.toList());
            log.info("Succeed");
            Collections.reverse(messageList);
            return messageList;
        } catch (DataAccessException d) {
            log.error("ERROR: " + d.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find messages" + d);
        }
    }

    @GetMapping(value = "/api/message/noRead")
    public Long getNumberOfNotReadMessages(HttpServletRequest r) {
        try {
            log.info("Getting number of not read messages");
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            UserTW user = userService.findUserById(userId);
            Long notReadMessages = (messageService.findMessagesByUserId(user)).stream().filter(m -> !m.getRead())
                    .count();

            log.info("Succeed");
            return notReadMessages;
        } catch (DataAccessException d) {
            log.error("ERROR: " + d.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find messages");
        }
    }

    @GetMapping(value = "/api/message/noReadByTag")
    public Long getNumberOfNotReadMessagesByTag(HttpServletRequest r, @RequestParam(required = true) int tagId) {
        try {
            log.info("Getting number of not read messages by tag");
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            Tag tag = tagService.findTagById(tagId);
            UserTW user = userService.findUserById(userId);
            Long notReadMessages = (messageService.findMessagesByTag(user, tag)).stream().filter(m -> !m.getRead())
                    .count();

            log.info("Succeed");
            return notReadMessages;
        } catch (DataAccessException d) {
            log.error("ERROR: " + d.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find messages");
        }
    }

    @PostMapping(value = "api/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> newMessage(HttpServletRequest r, @Valid @RequestPart("message") Message message,
            @RequestPart("files") MultipartFile[] files) {
        try {
            log.info("Creating new message");
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            UserTW sender = userService.findUserById(userId);
            if (sender == null)
                return ResponseEntity.badRequest().build();
            message.setSender(sender);
            message.setRead(false);

            log.info("Setting the recipient list");
            List<UserTW> recipientList = message.getRecipientsEmails().stream()
                    .map(mail -> userService.findByEmail(mail)).collect(Collectors.toList());
            message.setRecipients(recipientList);
            messageService.saveMessage(message);

            if (message.getToDoList() != null) {
                log.info("Todo list received. saving the relationships");
                List<ToDo> toDoList = message.getToDoList().stream().map(todoId -> toDoService.findToDoById(todoId))
                        .collect(Collectors.toList());
                message.setToDoList(null);
                message.setToDos(new ArrayList<>());
                message.getToDos().addAll(toDoList);
                messageService.saveMessage(message);
                for (ToDo todo : toDoList) {
                    todo.getMessages().add(message);
                    toDoService.saveToDo(todo);
                }
            }

            if (message.getTagList() != null) {
                log.info("Tag list received. saving the relationships");
                List<Tag> tagList = message.getTagList().stream().map(tagId -> tagService.findTagById(tagId))
                        .collect(Collectors.toList());
                message.setTagList(null);
                message.setTags(new ArrayList<>());
                message.getTags().addAll(tagList);
                messageService.saveMessage(message);
                for (Tag tag : tagList) {
                    tag.getMessages().add(message);
                    tagService.saveTag(tag);
                }
            }

            if (files != null) {
                log.info("File received. attaching it to the message");

                for (MultipartFile f : files) {
                    log.info("Attaching a file");
                    Attachment attach = new Attachment();
                    attach.setFile(f);
                    newAttachment(attach, message);
                }

            }

            log.info("Message sent correctly");
            return ResponseEntity.ok().build();
        } catch (DataAccessException | TagLimitProjectException | ToDoMaxToDosPerAssigneeException | IOException
                | TooBigFileException d) {
            log.error("exception ocurred saving message", d);
            return ResponseEntity.badRequest().build();
        }
    }

    public void newAttachment(Attachment attach, Message msg)
            throws DataAccessException, IOException, TooBigFileException {

        log.info("Saving the attachment");
        attach.setMessage(msg);
        attachmentService.uploadAndSaveAttachment(attach);
    }

    @PostMapping(value = "api/message/reply")
    public ResponseEntity<String> replyMessage(HttpServletRequest r,
            @Valid @RequestBody(required = true) Message message,
            @RequestParam(required = true) Integer repliedMessageId) {
        log.info("Replying to the message with id: " + repliedMessageId);
        Message repliedMessage = messageService.findMessageById(repliedMessageId);
        message.setReplyTo(repliedMessage);
        log.info("Creating the reply");
        return newMessage(r, message, null);

    }

    @PostMapping(value = "api/message/forward")
    public ResponseEntity<String> forwardMessage(HttpServletRequest r, @RequestBody List<String> forwardList,
            @RequestParam(required = true) Integer forwardedMessageId) {
        log.info("Forwarding the message with id: " + forwardedMessageId + " to: " + forwardList);
        Message message = messageService.findMessageById(forwardedMessageId);
        log.info("Copying the message");
        Message forwardCopy = new Message();
        forwardCopy.setRead(false);
        forwardCopy.setRecipientsEmails(forwardList);
        forwardCopy.setSubject(message.getSubject().contains("Fw(") ? message.getSubject()
                : "Fw(" + message.getSender().getName() + "): " + message.getSubject());
        forwardCopy.setText(message.getSender().getName() + "'s Original Message: " + message.getText());
        forwardCopy.setTagList(message.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toList()));
        forwardCopy.setToDoList(message.getToDos().stream().map(todo -> todo.getId()).collect(Collectors.toList()));
        forwardCopy.setAttatchments(List.copyOf(message.getAttatchments()));
        log.info("Creating the forward copy");
        return newMessage(r, forwardCopy, null);
    }

    @PostMapping(value = "/api/message/markAsRead")
    public ResponseEntity<String> markAsRead(HttpServletRequest r, @RequestParam(required = true) Integer messageId) {
        try {
            log.info("Marking a message as read");
            Integer userId = (Integer) r.getSession().getAttribute("userId");
            UserTW user = userService.findUserById(userId);
            Message message = messageService.findMessageById(messageId);
            if (message.getRecipients().contains(user)) {
                message.setRead(true);

                log.info("Succeed, saving message");
                messageService.saveMessage(message);
            } else {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (DataAccessException d) {
            return ResponseEntity.badRequest().build();
        }
    }
}
