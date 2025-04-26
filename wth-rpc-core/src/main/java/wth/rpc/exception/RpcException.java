package wth.rpc.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcException extends Exception{
    private String message;

    private Integer code;
    public RpcException(String message) {
        super(message);
    }
}
