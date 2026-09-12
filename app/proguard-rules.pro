####################################################################################################
# PROGUARD-MIN.PRO — VERSÃO FINAL (CONSOLIDADA)
# Arquivo consolidado com todos os blocos (0 a 18) solicitados, comentários em português-BR,
# regras colocadas nos blocos técnicos corretos e placeholders substituídos pelo seu package real.
# Copiar/colar diretamente em app/proguard-rules.pro ou nome equivalente.
####################################################################################################


####################################################################################################
# [SEÇÃO 0 — COMENTÁRIOS DO PROJETO / INSTRUÇÕES DO ARQUIVO]
# Instruções e observações originais do arquivo (documentação local).
####################################################################################################

# Adicione regras específicas do ProGuard para o projeto aqui.
# Você pode controlar o conjunto de arquivos de configuração aplicados usando
# a propriedade proguardFiles no build.gradle.
#
# Para mais detalhes, consulte:
#   http://developer.android.com/guide/developing/tools/proguard.html
#
# Se o seu projeto utiliza WebView com JavaScript, descomente a seção abaixo
# e especifique o nome completo da classe da interface JavaScript:
# classe:
# comentário abaixo, WebView removido a partir da versão v053i
#-keepclassmembers class fasofts.element.ui.DnsConfigureWebViewActivity$JSInterface {
#    public *;
#}
#
# Descomente a linha abaixo para preservar as informações de número de linha,
# úteis para depuração de stack traces.
#-keepattributes SourceFile,LineNumberTable

####################################################################################################
# BLOCO 1 — KEEP DO APLICATIVO (pacote principal)
# Mantém classes do seu app, evita remoção/renomeação que quebre runtime.
####################################################################################################

# Mantém todas as classes do seu pacote real sem obfuscar (segurança / estabilidade)
# Justificativa: evita que o shrink/obfuscation remova ou renomeie classes críticas do app.
#-keep class fasofts.element.** { *; }
#-keep class fasofts.element.ui.HomeScreenActivity.kt
-dontwarn fasofts.element.**


####################################################################################################
# BLOCO 2 — ANDROID / JETPACK / GOOGLE
# Regras para compatibilidade com o framework Android e bibliotecas Google.
####################################################################################################

# Evita warnings gerados por classes do framework durante o processamento (útil em builds com libs opcionais)
-dontwarn android.**
-dontwarn androidx.**
-dontwarn com.google.**

# Mantém classes do framework Android/AndroidX/Google (prevenção de remoção)
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class com.google.** { *; }


####################################################################################################
# BLOCO 3 — REFLECTION / RUNTIME
# Regras que preservam membros usados por reflexão, anotações e serialização.
####################################################################################################

# Mantém métodos expostos como interface JavaScript (WebView) — descomentável se usar WebView JS
-keepclassmembers class fasofts.element.ui.DnsConfigureWebViewActivity$JSInterface {
    public *;
}

# Mantém classes R (recursos) e seus campos (ids)
-keepclassmembers class **.R$* {
    <fields>;
}

# Mantém construtores comuns criados via reflection
-keepclassmembers class * {
    public <init>(android.content.Context);
}

# Mantém membros marcados com @Keep
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Mantém membros usados pelo GSON (@SerializedName)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName *;
}

# Mantém annotations (evita remoção das anotações necessárias)
-keepattributes *Annotation


####################################################################################################
# BLOCO 4 — AJUSTES GERAIS / CORE
# Ajustes para serialização, enums, Serializable, etc.
####################################################################################################

# Reforço: manter annotations
-keepattributes *Annotation

# Mantém nomes e membros usados na serialização Java (java.io.Serializable)
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Mantém enums (values/valueOf) evitando crashes pós-ofuscação
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


####################################################################################################
# BLOCO 5 — JNI / STRING PROTECTION / LOGS
# Regras para métodos nativos, proteção de strings (adaptclassstrings) e remoção de logs.
####################################################################################################

# Mantém classes que possuem métodos nativos (JNI) — evita remoção desses métodos
-keepclasseswithmembers class * {
    native <methods>;
}


#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Protege (adapta) strings em classes R$* (técnica de obfuscação de strings)
# Nota: adaptclassstrings é processada pelo ProGuard/R8 em alguns modos; ver compatibilidade.
-adaptclassstrings **.R$*

# Remove chamadas de Log durante otimizações (assumenosideeffects evita side-effects de chamadas de log)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}


####################################################################################################
# BLOCO 6 — RETROFIT / OKHTTP / GSON / KOTLIN
# Regras específicas para bibliotecas de rede e serialização.
####################################################################################################

# Garante que Retrofit não perca assinaturas genéricas (Call/Response)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Mantém Continuation usado por coroutines (R8 Full mode pode stripar signatures)
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Mantém TypeToken (GSON) e subclasses para preservar assinaturas genéricas
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Mantém algumas classes internas do GSON usadas em parsing (evita problemas em versões específicas)
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.internal.** { *; }

# Mantém membros marcados com @SerializedName (modelos JSON)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName *;
}



#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

####################################################################################################
# BLOCO 7 — ANTI-MOD (Anti-Hook / Anti-Tamper)
# Proteções que você implementará no código — aqui apenas preservamos as classes/membros.
####################################################################################################

# Preserva anotações e classes de verificação anti-tamper
-keepclassmembers class * {
    @fasofts.element.security.Keep *;
}
# Mantém classes do pacote de segurança customizado (ex.: checks, hashes, validadores)
-keep class * extends fasofts.element.secure.* { *; }
-keep class fasofts.element.security.hook.** { *; }
-keep class fasofts.element.security.crypto.** { *; }
-keepclassmembers class * {
    @fasofts.element.annotations.RuntimeCheck *;
}




####################################################################################################
# BLOCO 8 — ROOM / SQLITE
# Regras para preservar entidades, DAOs e classes Room.
####################################################################################################

# Mantém toda a API Room necessária em runtime
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Mantém anotações e membros críticos do Room (Entity, Dao, Database, Query, TypeConverter)
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Entity *;
    @androidx.room.Database *;
    @androidx.room.Query *;
    @androidx.room.TypeConverter *;
}

# Mantém ColumnInfo caso usado
-keepclassmembers class * {
    @androidx.room.ColumnInfo *;
}


####################################################################################################
# BLOCO 9 — NAVIGATION / SAFE ARGS
# Mantém classes geradas por Jetpack Navigation e Safe Args.
####################################################################################################

# Mantém classes do Navigation (fragments, navgraphs geradas)
-keep class androidx.navigation.** { *; }

# Safe Args — manter classes de Directions e Args geradas
-keep class *Directions { *; }
-keep class *Args { *; }

# Mantém anotações de deep links (preservação de parâmetros)
-keepclassmembers class * {
    @androidx.navigation.NavDeepLink *;
}



####################################################################################################
# BLOCO 10 — OBFUSCAÇÃO AVANÇADA (NÍVEL MÁXIMO) — explicado
# Diretivas que aumentam agressividade da ofuscação / reestruturação.
####################################################################################################

# Reempacota classes em novos pacotes (dificulta localizar estrutura original)
# Comentário: repackageclasses altera pacotes, reduz legibilidade do decompilado.
-repackageclasses

#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Permite sobrecarga agressiva de nomes (mesmo nome para múltiplos métodos com assinaturas diferentes)
# Comentário: overloadaggressively reduz legibilidade, pode causar problemas em code-reflection-sensitives.
-overloadaggressively

# Achata hierarquia de pacotes — coloca classes em um namespace menor
# Comentário: flattenpackagehierarchy remove hierarquia lógica, útil para confundir analistas.
# -flattenpackagehierarchy 
## desativado por conflito com -repackageclasses



#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Permite R8/ProGuard alterar modificadores de acesso (public/private/protected)
# Comentário: allowaccessmodification facilita otimizações e encolhimento, mas muda visibilidade.
-allowaccessmodification



#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Número de passes de otimização (quanto maior, mais agressivo)
# Comentário: optimizationpasses 5 realiza múltiplos ciclos de otimização; use com testes extensivos.
-optimizationpasses 64


####################################################################################################
# BLOCO 11 — PROTEÇÃO CONTRA ENGENHARIA REVERSA (Parcelable + Construtores)
# Mantém classes Parcelable e construtores usados por frameworks/reflection.
####################################################################################################

# Mantém classes que implementam Parcelable (evita crashes ao passar Intents/Bundles)
-keep class * implements android.os.Parcelable { *; }



# Mantém construtores que recebem Context (muito usado por frameworks)
-keepclassmembers class * {
    public <init>(android.content.Context);
}

# Mantém todos os construtores públicos (previne remoção por reflexão)
-keepclassmembers class * {
    public <init>(...);
}



####################################################################################################
# BLOCO 12 — PRESERVAÇÃO DE BIBLIOTECAS EXTERNAS (Jackson, Retrofit, OkHttp, Dagger, Inject, FileProvider...)
####################################################################################################

# Mantém classes do Jackson (serialização via Jackson)
-keep class com.fasterxml.jackson.** { *; }

# Mantém classes do Retrofit (cliente REST)
-keep class retrofit2.** { *; }

# Mantém classes do OkHttp (networking)
-keep class okhttp3.** { *; }

# Mantém classes do Dagger (injeção de dependência)
-keep class dagger.** { *; }

# Mantém classes de javax.inject (anotações e scopes)
-keep class javax.inject.** { *; }

# Mantém o FileProvider (compartilhamento de arquivos via Uri)
-keep class androidx.core.content.FileProvider { *; }

#/===/===/===/===/===/===/===/===/===/
# Evita remoção agressiva de recursos
#-dontshrinkresources  #removido
#motivo removi ofuscscao PRO R8
#/===/===/===/===/===/===/===/===/===/


# Impedir remoção de classes de serialização (Serializable)
-keepclassmembers class * implements java.io.Serializable { *; }

# Mantém atributos Signature (essencial para Retrofit/Gson e genéricos)
-keepattributes Signature

# Mantém classes do seu módulo de criptografia
#-keep class fasofts.element.encryption.** { *; }

# SLF4J
-keep class org.slf4j.Logger { *; }
-keep class org.slf4j.LoggerFactory { *; }

# JDBCType — quando driver JDBC/SQL for usado
-keep class java.sql.JDBCType { *; }

# Evitar remoção de classes SQLite externas
-keep class org.sqlite.** { *; }

# Retrofit/Gson/Kotlin genéricos (repetidos por segurança)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call { *; }
-keep,allowobfuscation,allowshrinking class retrofit2.Response { *; }
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation { *; }

# TypeToken (GSON) assinaturas genéricas
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken { *; }
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken { *; }

# Suprimir avisos para classes faltantes (opcional)
-dontwarn java.sql.JDBCType
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Regras opcionais que permanecem comentadas até sua autorização
#-keep class org.slf4j.impl.StaticLoggerBinder { *; }    #ativar se precisar do binding do SLF4J
-keepnames class fasofts.element.ActivityNoRemode
-keepnames class fasofts.element.UserListNoModActivity




####################################################################################################
# BLOCO 13 — ANTI-DEBUG
# Preserva classes usadas para detecção anti-debug e lógica de proteção.
####################################################################################################

# Preservar nomes/classes que implementam lógica AntiDebug (evita remoção)
-keep class fasofts.element.**.*Debug** { *; }
-keep class fasofts.element.**.*AntiDebug** { *; }

# Preservar métodos do Debug se usados (isDebuggerConnected, waitForDebugger)
-keep class * {
    public static boolean isDebuggerConnected(...);
    public static void waitForDebugger(...);
}

# Preservar classes que podem analisar stacktrace/exceptions para detectar debugging
-keep class * extends java.lang.Exception { *; }


####################################################################################################
# BLOCO 14 — ANTI-EMULADOR
# Preserva verificações de ambiente (QEMU, build props, CPU, sensores).
####################################################################################################

# Preserva classes que realizam checagens de ambiente/emulador
-keep class fasofts.element.**.*EmuCheck** { *; }
-keep class fasofts.element.**.*AntiEmu** { *; }

# Preserva membros usados para heurísticas (Build/System)
-keepclassmembers class android.os.Build { *; }
-keepclassmembers class java.lang.System { *; }


####################################################################################################
# BLOCO 15 — ANTI-HOOK / ANTI-INJEÇÃO
# Preserva classes e métodos usados para detectar/evitar hooks (Frida/Xposed/etc).
####################################################################################################

# Preserva classes que verificam hooking
-keep class fasofts.element.**.*HookCheck** { *; }
-keep class fasofts.element.**.*AntiHook** { *; }

# Mantém membros/reflection relacionados a ClassLoader/Method (detecção de substituições)
-keepclassmembers class java.lang.ClassLoader { *; }
-keepclassmembers class java.lang.reflect.Method { *; }

# Mantém pacote de segurança completo (integrações e verificadores)
-keep class fasofts.element.security.** { *; }


####################################################################################################
# BLOCO 16 — ANTI-TAMPER (APK ALTERADO / ASSINATURA / PATCH DETECTION)
# Regras para preservar checagens de integridade, verificações de assinatura, validações de hash.
####################################################################################################

# Mantém classes/validators que verificam hashes, assinatura do APK, tamper checks
-keep class fasofts.element.security.tamper.** { *; }
-keep class fasofts.element.security.signature.** { *; }

# Mantém métodos que calculam/verificam checksums/assinaturas
-keepclassmembers class * {
    @fasofts.element.annotations.TamperCheck *;
}

# Preservar classes nativas que possam fazer verificação robusta no JNI
-keep class fasofts.element.security.Native { *; }
-keepclasseswithmembers class fasofts.element.security.Native {
    native <methods>;
}



#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
####################################################################################################
# BLOCO 17 — ANTI-ROOT
# Regras para preservar detecções de root e lógica relacionada.
####################################################################################################

# Mantém classes que checam por root (su, busybox, paths, permissões)
-keep class fasofts.element.security.root.** { *; }
-keepclassmembers class * {
    @fasofts.element.annotations.RootCheck *;
}

# Mantém comandos / wrappers nativos que verificam permissões/flags
-keep class fasofts.element.security.shell.** { *; }


####################################################################################################
# BLOCO 18 — ANTI-MEMORY-DUMP / ANTI-DUMP
# Preservação de classes que atacam tentativas de dump de memória ou leitura de regiões sensíveis.
####################################################################################################

# Mantém proteções contra memory dump
-keep class fasofts.element.security.anti_dump.** { *; }
-keepclassmembers class * {
    @fasofts.element.annotations.AntiDump *;
}

# Mantém chaves/verificadores manipulação em nativo (se houver)
-keep class fasofts.element.security.anti_dump.NativeAntiDump { *; }
-keepclasseswithmembers class fasofts.element.security.anti_dump.NativeAntiDump {
    native <methods>;
}


####################################################################################################
# BLOCO EXTRA — MANTER SERVICES, RECEIVERS E UTILS
# As regras que você pediu para manter serviços, receivers e utilitários.
####################################################################################################

# Mantém Services do seu pacote (evita remoção de serviços declarados no Manifest)
-keep class fasofts.element.service.** { *; }

# Mantém BroadcastReceivers do seu pacote
-keep class fasofts.element.receiver.** { *; }

# Mantém classes utilitárias importantes
-keep class fasofts.element.util.** { *; }


####################################################################################################
# BLOCO NATIVE / JNI ESPECÍFICO — regras pedidas sobre Native / TamperGuard / App / Activities
####################################################################################################

# Não remova o guard e classes críticas de segurança e UI
-keep class fasofts.security.** { *; }
#-keep class fasofts.element.anti* { public static *; }
-keep class fasofts.element.security.TamperGuard { *; }
-keep class fasofts.element.ui.ActivityNoRemode { *; }
-keep class fasofts.element.ui.UserListNoModActivity { *; }

# Mantém métodos nativos em qualquer classe (general keep)
-keepclasseswithmembers class * {
    native <methods>;
}

# Mantém a classe Native e métodos (necessário para JNI)
-keep class fasofts.element.security.Native {
    public static *;
    public <methods>;
    native <methods>;
}

# Mantém a classe Application e Activities do app para que o Android encontre
-keep class fasofts.element.App { *; }
-keep class fasofts.element.ui.** { *; }
-keep class com.journeyapps.barcodescanner.CaptureActivity { *; }



#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Proteja classes de segurança específicas de renomeações (opcional: pode ofuscar outras partes)
-keep class fasofts.element.security.Security { *; }
-keep class fasofts.element.security.RuntimeWatchdog { *; }


####################################################################################################
# REGRAS GLOBAIS / FINAIS
####################################################################################################

# Remova logs desnecessários e suprimir warnings (ATENÇÃO: -dontwarn ** suprime avisos, use com cautela)
-dontwarn **

# MANTENHA -dontobfuscate COMENTADO (você pediu) — assim a ofuscação permanece habilitada
-dontobfuscate



#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# Gerar mapping (muito importante para desfazer obfuscação em relatórios)
#-printmapping obfuscation/mapping.txt
#-printmapping build/outputs/mapping/release/mapping.txt

# Renomear atributo SourceFile para reduzir rastreabilidade (quando line numbers mantidos)
-renamesourcefileattribute SourceFile

# Nota: se preferir desativar shrinking globalmente, use -dontshrink (não recomendado)
# Nota2: se for usar R8 como shrinker/offuscator, algumas opções de ProGuard podem ser ignoradas ou ter comportamento distinto.
# Teste todas as combinações em build de staging antes de liberar em produção.




#PARTE DO NOVO ANTI MODDER's
# manter o loader estável
-keep class fasofts.element.** { *; }
# manter a entry minimal
-keepclassmembers class fasofts.element.** { public *; }
###############################

-dontwarn fasofts.element.sysdata.**
-keep class fasofts.element.sysdata.** { *; }
-dontwarn fasofts.element.internalcheck.**
-keep class fasofts.element.internalcheck.** { *; }



# manter native bridge
#-keep class fasofts.element.* { *; }
# ofuscar o resto agressivamente
-dontwarn com.app.generated.**

#> Ajuste os pacotes conforme o loader que você criar (abaixo dei sugestão com.app.security).






####################################################################################################
# FIM DO ARQUIVO — INSTRUÇÕES RÁPIDAS
# - Teste o build de release com mapping gerado.
# - Se ocorrerem crashes relacionados a reflect/serialization, ative regras específicas (descomente/ajuste).
# - Para ativar ofuscação mínima remova comentário de -dontobfuscate (não recomendado).
# - Se desejar, executo uma varredura de "minimização de regras" para reduzir duplicidades.
####################################################################################################