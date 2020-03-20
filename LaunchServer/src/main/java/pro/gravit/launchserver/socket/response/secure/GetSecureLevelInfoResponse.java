package pro.gravit.launchserver.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.events.request.GetSecureLevelInfoRequestEvent;
import pro.gravit.launchserver.auth.protect.interfaces.SecureProtectHandler;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

public class GetSecureLevelInfoResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if(!(server.config.protectHandler instanceof SecureProtectHandler))
        {
            GetSecureLevelInfoRequestEvent response = new GetSecureLevelInfoRequestEvent(null);
            response.enabled = false;
            sendResult(response);
        }
        SecureProtectHandler secureProtectHandler = (SecureProtectHandler) server.config.protectHandler;
        if(!secureProtectHandler.allowGetSecureLevelInfo(client))
        {
            sendError("Permissions denied");
            return;
        }
        if(client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
        if(client.trustLevel.verifySecureKey == null) client.trustLevel.verifySecureKey = secureProtectHandler.generateSecureLevelKey();
        GetSecureLevelInfoRequestEvent response = new GetSecureLevelInfoRequestEvent(client.trustLevel.verifySecureKey);
        response.enabled = true;
        sendResult(secureProtectHandler.onGetSecureLevelInfo(response));
    }
}
