package crfa.app.commands;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Optional;

import static crfa.app.util.ConsoleWriter.strLn;
import static crfa.app.util.ConsoleWriter.writeLn;

@ShellComponent
@Slf4j
public class NetworkCommands {

    private Optional<Network> activeNetwork = Optional.of(Network.PREVIEW);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public enum Network {
        MAINNET,
        TESTNET,
        PREPROD,
        PREVIEW
    }

    public Optional<Network> getActiveNetwork() {
        return activeNetwork;
    }

    @ShellMethod(value = "Current active network", key = "current-network")
    public void currentNetwork() {
        if (activeNetwork.isPresent()) {
            printNetwork(activeNetwork.orElseThrow());
            return;
        }

        val sb = new StringBuilder();

        sb.append(strLn("No network selected yet, please use: switch-network command."));

        writeLn(sb.toString());
    }

    @ShellMethod(value = "Switches active network", key = "switch-network")
    public void switchNetwork(@ShellOption(value = {"-n"}, defaultValue = "testnet", help = "Provide a known network (mainnet, testnet, preprod, preview)") String network) {
        val n = Network.valueOf(network.toUpperCase());
        this.activeNetwork = Optional.of(n);

        printNetwork(n);
        applicationEventPublisher.publishEvent(new NetworkSwitchedEvent(this, n));
    }

    private static void printNetwork(Network network) {
        val sb = new StringBuilder();

        sb.append(strLn("Name: %s", network.name()));

        writeLn(sb.toString());
    }

    public final class NetworkSwitchedEvent extends ApplicationEvent {

        private final Network network;

        public NetworkSwitchedEvent(Object source, Network network) {
            super(source);
            this.network = network;
        }

        public Network getNetwork() {
            return network;
        }

    }

}
