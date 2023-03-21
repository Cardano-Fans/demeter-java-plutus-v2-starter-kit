# demeter-java-plutus-v2-starter-kit
Demeter template to start Offchain Cardano Plutus V2 development with Java.

## This template will demonstrate a few basic Cardano functions such as
- accounts management via simple wallet
- invoking smart contracts based on a simple GuessGame contract.

We will be using one of the simplest (albeit centralised) methods which is via KOIOS API. By default cli app will use PREVIEW testnet.

Upon opening project make sure to run: ```CardanoJavaStarterKit``` with at least JDK 17 LTS.

[![Code in Cardano Workspace](https://demeter.run/code/badge.svg)](https://demeter.run/code?repository=https://github.com/Cardano-Fans/demeter-java-plutus-v2-starter-kit&template=java)

## Account generation

Start application and invoke:
```shell
shell:>gen-new-account
Name      : Doug_Updegrave
Address   : addr_test1qra5hzp7q5wpx86vq34e6wts79hw86geet3ds2apuz5am7pqe4dmag83thfaqdwfgwrrk0duhwy92f6chgpr9937sktq2nk4cn
Mnemonics : hill october gesture tumble skull dice favorite leg lake tool perfect object hurdle asthma timber cotton street boring broken switch modify come ring crunch
```

List accounts:
```shell
shell:>list-accounts
Name      : Doug_Updegrave
Address   : addr_test1qra5hzp7q5wpx86vq34e6wts79hw86geet3ds2apuz5am7pqe4dmag83thfaqdwfgwrrk0duhwy92f6chgpr9937sktq2nk4cn
Mnemonics : hill october gesture tumble skull dice favorite leg lake tool perfect object hurdle asthma timber cotton street boring broken switch modify come ring crunch
```

Restore account:
```shell
shell:>restore-account -n "Rick_Shaw" -m "raw orient unknown junk various amused nest install sentence output funny slide concert panda wink job tackle dust exhaust embark mixture plug general mention"
Name      : Rick_Shaw
Address   : addr_test1qr0nrf2qchstgggv39da6unkwtarspe4tlrt6wc6sxupadpp2sa27xajzjt0twvst6c95pptefndu2xpfh8v6f55m0xqj93h9u
Mnemonics : raw orient unknown junk various amused nest install sentence output funny slide concert panda wink job tackle dust exhaust embark mixture plug general mention
```

Now let's load some balance on preview network to your test account. For this we will use free facet: https://docs.cardano.org/cardano-testnet/tools/faucet

Once faucet sends funds we should be able to see them via:
```shell
shell:>account-balance -n Rick_Shaw
Account's Rick_Shaw balance: 4 ADA
```
Alternatively if you don't have account configured you can just check balance for bech32 address:

```shell
shell:>address-balance -a addr_test1qrw5me8wf2s8rch2tc09ctmfspsrw53kjta7y7trjayl79r06gx4andcfwpfxa65kxhljsjgw3np9j779fqdg3u3kgpq3uhqew
Address's addr_test1qrw5me8wf2s8rch2tc09ctmfspsrw53kjta7y7trjayl79r06gx4andcfwpfxa65kxhljsjgw3np9j779fqdg3u3kgpq3uhqew balance: 9994 ADA
```

Now let's demonstrate invoking PlutusV2 smart contract. We will invoke a simple GuessGame, which has already been deployed to Cardano's preview net.
In this example we will use:
- CIP-40: collateral outputs to prevent necessity for a user to specify collateral input
- Aiken: we will use Aiken to evaluate on the client side ExUnits (costs) for invoking redeemers

First we need to lock funds for this let's:
```shell
shell:>lock-ada -f Rick_Shaw -a 5
```
This will lock 5 ADA for Rick Shaw's account in a smart contract: addr_test1wzcppsyg36f65jydjsd6fqu3xm7whxu6nmp3pftn9xfgd4ckah4da

After you get successful message locking ADA, we can unlock it with the following command:

```shell
shell:>claim-ada -f Rick_Shaw
```

Now you should receive your 5 ADA back!

Make sure to navigate to preview environment and check: https://preview.cardanoscan.io/address/70b010c0888e93aa488d941ba4839136fceb9b9a9ec310a573299286d7