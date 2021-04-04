package group.bison.data.data_layer.intake.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by diaobisong on 2020/7/2.
 */
@Data
public class MetricsMessageDto implements Serializable {

    private static final long serialVersionUID = -1L;

    private String id;

    private String topic;

    private String from;

    private String to;

    private MessageBody body;

    private Long timestamp = System.currentTimeMillis();

    @Data
    public static class MessageBody implements Serializable {

        private String content;

        private String configId;
    }
}
