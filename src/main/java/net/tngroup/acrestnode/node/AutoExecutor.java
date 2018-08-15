package net.tngroup.acrestnode.node;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import net.tngroup.acrestnode.nodeclient.NodeClient;
import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import net.tngroup.acrestnode.nodeclient.components.SettingComponent;
import net.tngroup.acrestnode.nodeclient.components.StatusSenderComponent;
import net.tngroup.acrestnode.nodeclient.components.TaskComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AutoExecutor implements ApplicationRunner {

    private NodeClient nodeClient;
    private ChannelComponent channelComponent;
    private TaskComponent taskComponent;
    private StatusSenderComponent statusSenderComponent;
    private SettingComponent settingComponent;
    private Processor processor;

    @Autowired
    AutoExecutor(NodeClient nodeClient,
                 ChannelComponent channelComponent,
                 TaskComponent taskComponent,
                 StatusSenderComponent statusSenderComponent,
                 SettingComponent settingComponent,
                 Processor processor) {
        this.nodeClient = nodeClient;
        this.channelComponent = channelComponent;
        this.taskComponent = taskComponent;
        this.statusSenderComponent = statusSenderComponent;
        this.settingComponent = settingComponent;
        this.processor = processor;
    }

    @Override
    public void run(ApplicationArguments args) {
        settingComponent.checkCode();
        settingComponent.checkKey();
        settingComponent.checkStatus();
        settingComponent.checkSettings();

        if (channelComponent.isStatus()) processor.doCommand("start");

        nodeClient.createBootstrap(new Bootstrap(), new NioEventLoopGroup());

        taskComponent.start();
        statusSenderComponent.start();
    }
}