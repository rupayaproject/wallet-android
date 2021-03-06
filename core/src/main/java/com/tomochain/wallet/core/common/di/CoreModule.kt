package com.tomochain.wallet.core.common.di

import android.content.Context
import com.tomochain.wallet.core.components.*
import com.tomochain.wallet.core.habak.HabakFactory
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.DatabaseWalletSecret
import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainService
import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainServiceImpl
import com.tomochain.wallet.core.w3jl.components.signer.SignerService
import com.tomochain.wallet.core.w3jl.components.signer.SignerServiceImpl
import com.tomochain.wallet.core.w3jl.components.tomochain.token.*
import com.tomochain.wallet.core.wallet.WalletSecretDataImpl
import com.tomochain.wallet.core.wallet.WalletSecretDataService
import com.tomochain.wallet.core.wallet.WalletService
import com.tomochain.wallet.core.wallet.WalletServiceImpl
import dagger.Module
import dagger.Provides
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.lang.ref.WeakReference
import javax.inject.Singleton

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Module
class CoreModule(var context: WeakReference<Context>,
                 var config: CoreConfig = DefaultConfig()) {

    @Singleton
    @Provides
    fun getHaBak() : Habak {
        return config.cryptographyManager ?: HabakFactory(context.get()).withAlias(config.cryptoAlias).build()
    }

    @Singleton
    @Provides
    fun getWalletSecretDataService() : WalletSecretDataService {
        return WalletSecretDataImpl(getHaBak(), getDatabaseWallet())
    }

    @Singleton
    @Provides
    fun getDatabaseWallet() : DatabaseWalletSecret {
        return DatabaseWalletSecret.getInstance(context.get()!!, config.roomAlias)!!
    }


    @Singleton
    @Provides
    fun getWalletService() : WalletService{
        return WalletServiceImpl(getHaBak())
    }

    @Singleton
    @Provides
    fun getWeb3JService() : Web3j {
        return Web3j.build(HttpService(config.chain?.getEndpoint()))
    }

    @Provides
    fun getCoreFunctions() : CoreFunctions {
        return CoreFunctionsImpl( getDatabaseWallet(), getWalletService())
    }

    @Provides
    fun getSignerService() : SignerService {
        return SignerServiceImpl(null, getWalletSecretDataService(), getWeb3JService())
    }

    @Provides
    fun getCoreBlockChainService() : BlockChainService {
        return BlockChainServiceImpl(null, getCoreFunctions(), getWalletSecretDataService(), getWeb3JService())
    }

    @Provides
    fun getTokenService() : TokenService{
        return TokenServiceImpl(null, getWeb3JService(), config.chain)
    }



    @Provides
    fun getTRC20Service() : TRC20Service {
        return TRC20ServiceImpl(null,
                getWeb3JService(),
                config.chain,
                getWalletSecretDataService(),
            getCoreBlockChainService())
    }


    @Provides
    fun getTRC21Service() : TRC21Service {
        return TRC21ServiceImpl(null,
            getWeb3JService(),
            config.chain,
            getWalletSecretDataService(),
            getCoreBlockChainService())
    }


    @Provides
    fun getTokenManager() : TokenManagerService {
        return TokenManagerImpl(
            TokenServiceImpl(null, getWeb3JService(), config.chain),
            getTRC20Service(), getTRC21Service()
        )
    }


    @Provides
    fun getWalletFunctions() : WalletFunctions {
        return WalletFunctionsImpl(
            getCoreBlockChainService(), getSignerService()
        )
    }
}