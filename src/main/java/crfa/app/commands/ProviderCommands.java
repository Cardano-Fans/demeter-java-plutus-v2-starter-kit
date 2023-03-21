package crfa.app.commands;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.koios.KoiosBackendService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static crfa.app.util.ConsoleWriter.strLn;
import static crfa.app.util.ConsoleWriter.writeLn;

@ShellComponent
@Slf4j
public class ProviderCommands implements ApplicationListener<ApplicationEvent> {

    private final static ProviderType DEFAULT_PROVIDER_TYPE = ProviderType.KOIOS;

    private Optional<ProviderType> activeProviderType = Optional.of(DEFAULT_PROVIDER_TYPE);

    private Optional<Provider> activeProvider = Optional.empty();

    @Autowired
    private NetworkCommands networkCommands;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof NetworkCommands.NetworkSwitchedEvent nse) {
            initProvider(Optional.of(DEFAULT_PROVIDER_TYPE), Optional.empty());
            writeLn(strLn("Switched network to: %s, reset to default provider type: %s", nse, DEFAULT_PROVIDER_TYPE));
        }
    }

    public record Provider(ProviderType providerType,
                           BackendService backendService,
                           Optional<String> key) { }

    @PostConstruct
    public void init() {
        initProvider(Optional.empty(), Optional.empty());
    }

    private void initProvider(Optional<ProviderType> activeProviderType, Optional<String> key) {
        networkCommands.getActiveNetwork().ifPresent(n -> {
            val provider = activeProviderType.or(() -> this.activeProviderType).orElseThrow();
            val url = urlForNetwork(provider, n).orElseThrow();
            this.activeProvider = Optional.of(new Provider(provider, new KoiosBackendService(url), key));
        });
    }

    public enum ProviderType {
        KOIOS,
        BLOCKFROST;
    }

    public Optional<Provider> getActiveProvider() {
        return activeProvider;
    }

    @ShellMethod(value = "Current provider", key = "current-provider")
    public void currentProvider() {
        if (activeProvider.isPresent()) {
            printProvider(activeProvider.orElseThrow());
            return;
        }

        val sb = new StringBuilder();

        sb.append(strLn("no provider selected yet: switch-provider command."));

        writeLn(sb.toString());
    }

    @ShellMethod(value = "Switches active provider", key = "switch-provider")
    public void switchProvider(@ShellOption(value = {"-n"}, defaultValue = "KOIOS", help = "Provide a known provider (koios, blockfrost)") String providerType,
                               @ShellOption(value = {"-k"}, defaultValue = "", help = "provider key") String key) {
        val pt = ProviderType.valueOf(providerType);
        val k = StringUtils.isBlank(key) ? Optional.<String>empty() : Optional.of(key);

        initProvider(Optional.of(pt), k);

        this.activeProvider.ifPresent(provider -> {
            printProvider(provider);
            applicationEventPublisher.publishEvent(new ProviderSwitchedEvent(this, provider));
        });
    }

    private static void printProvider(Provider provider) {
        val sb = new StringBuilder();

        sb.append(strLn("Provider Type: %s", provider.providerType.name()));

        provider.key().ifPresent(k -> {
            sb.append(strLn("Provider Key (if : %s", k));
        });

        writeLn(sb.toString());
    }

    public static Optional<String> urlForNetwork(ProviderCommands.ProviderType providerType, NetworkCommands.Network network) {
        if (providerType == ProviderCommands.ProviderType.KOIOS) {
            return switch (network) {
                case MAINNET -> Optional.of(com.bloxbean.cardano.client.backend.koios.Constants.KOIOS_MAINNET_URL);
                case PREPROD -> Optional.of(com.bloxbean.cardano.client.backend.koios.Constants.KOIOS_PREPROD_URL);
                case PREVIEW -> Optional.of(com.bloxbean.cardano.client.backend.koios.Constants.KOIOS_PREVIEW_URL);
                case TESTNET -> Optional.of(com.bloxbean.cardano.client.backend.koios.Constants.KOIOS_GUILDNET_URL);
            };
        }
        if (providerType == ProviderCommands.ProviderType.BLOCKFROST) {
            return switch (network) {
                case MAINNET ->
                        Optional.of(com.bloxbean.cardano.client.backend.blockfrost.common.Constants.BLOCKFROST_MAINNET_URL);
                case PREPROD ->
                        Optional.of(com.bloxbean.cardano.client.backend.blockfrost.common.Constants.BLOCKFROST_PREPROD_URL);
                case PREVIEW ->
                        Optional.of(com.bloxbean.cardano.client.backend.blockfrost.common.Constants.BLOCKFROST_PREVIEW_URL);
                case TESTNET ->
                        Optional.of(com.bloxbean.cardano.client.backend.blockfrost.common.Constants.BLOCKFROST_TESTNET_URL);
            };
        }

        return Optional.empty();
    }

    public final class ProviderSwitchedEvent extends ApplicationEvent {

        private final Provider provider;

        public ProviderSwitchedEvent(Object source, Provider provider) {
            super(source);
            this.provider = provider;
        }

        public Provider getProvider() {
            return provider;
        }

    }

}
