package crfa.app.commands;

import com.bloxbean.cardano.client.api.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.math.BigInteger;

import static com.bloxbean.cardano.client.common.ADAConversionUtil.lovelaceToAda;
import static crfa.app.util.ConsoleWriter.strLn;
import static crfa.app.util.ConsoleWriter.writeLn;

@ShellComponent
@Slf4j
public class BalanceCommands {

    @Autowired
    private ProviderCommands providerCommands;

    @Autowired
    private AccountCommands accountCommands;

    @ShellMethod(value = "Account's balance", key = "account-balance")
    public void accountBalance(@ShellOption(value = {"-n"}, help = "Provide account's name") String name) throws ApiException {
        val acc = accountCommands.findAccountByName(name).orElseThrow(() -> new RuntimeException(String.format("Account by name: %s not found", name)));
        val p = providerCommands.getActiveProvider().orElseThrow(() -> new RuntimeException("Provider not initialized!"));
        val sum = p.backendService().getAddressService().getAddressInfo(acc.baseAddress()).getValue().getAmount().stream().mapToLong(v -> Long.parseLong(v.getQuantity())).sum();

        writeLn(strLn("Account's %s balance: %d ADA", name, lovelaceToAda(BigInteger.valueOf(sum)).longValue()));
    }

    @ShellMethod(value = "Address's balance", key = "address-balance")
    public void addressBalance(@ShellOption(value = {"-a"}, help = "Provide address") String address) throws ApiException {
        if (!address.startsWith("addr")) {
            throw new RuntimeException("Doesn't seem like right address, ADA bech32 address should start from 'addr'!");
        }

        val p = providerCommands.getActiveProvider().orElseThrow(() -> new RuntimeException("Provider not initialized!"));
        val sum = p.backendService().getAddressService().getAddressInfo(address).getValue().getAmount().stream().mapToLong(v -> Long.parseLong(v.getQuantity())).sum();

        writeLn(strLn("Address's %s balance: %d ADA", address, lovelaceToAda(BigInteger.valueOf(sum)).longValue()));
    }

}
