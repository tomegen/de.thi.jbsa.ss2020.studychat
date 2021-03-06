package de.thi.jbsa.prototype.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import de.thi.jbsa.prototype.model.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Christopher Timm <christopher.timm@beskgroup.com> on 2020-04-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "messages")
public class MessageDoc {

  private Message message;
}
