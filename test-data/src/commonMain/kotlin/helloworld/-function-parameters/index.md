---
tags:
 - common
title: "FunctionParameters"
---


# FunctionParameters

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">class </span><a href="index.html">FunctionParameters</a>
</span></code></pre></div>Tests for function parameter rendering.

This class covers various types of function parameters:

 - Normal parameters


 - Parameters with default values


 - Vararg parameters


 - Lambda parameters (function types)


 - Lambda parameters with receiver


 - Lambda parameters with noinline modifier


 - Lambda parameters with crossinline modifier


 - Reified type parameters



</div>


## Constructors

### FunctionParameters

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">constructor</span><span class="p">(</span><span class="p">)</span>
</span></code></pre></div>
</div>

</div>


## Functions

### crossinlineLambda

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">inline </span><span class="kd">fun </span><a href="index.html#crossinlinelambda"><span class="nf">crossinlineLambda</span></a><span class="p">(</span><span class="kd">crossinline </span>action<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">)</span>
</span></code></pre></div>
</div>


<div markdown>

An inline function with crossinline lambda parameter.


</div>

</div>


### defaultParameters

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#defaultparameters"><span class="nf">defaultParameters</span></a><span class="p">(</span>host<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="o"> = </span><span class="s">"localhost"</span><span class="p">, </span>port<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html">Int</a><span class="o"> = </span><span class="nb">8080</span><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a>
</span></code></pre></div>
</div>


<div markdown>

A function with parameters that have default values.


</div>

</div>


### lambdaParameter

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#lambdaparameter"><span class="nf">lambdaParameter</span></a><span class="p">(</span>action<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">)</span>
</span></code></pre></div>
</div>


<div markdown>

A function with a lambda parameter.


</div>

</div>


### lambdaWithArguments

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#lambdawitharguments"><span class="nf">lambdaWithArguments</span></a><span class="p">(</span>transform<span class="o">: </span><span class="p">(</span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a>
</span></code></pre></div>
</div>


<div markdown>

A function with a lambda parameter that takes arguments.


</div>

</div>


### lambdaWithReceiver

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#lambdawithreceiver"><span class="nf">lambdaWithReceiver</span></a><span class="p">(</span>builder<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/-string-builder/index.html">StringBuilder</a><span class="p">.</span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a>
</span></code></pre></div>
</div>


<div markdown>

A function with a lambda parameter with receiver.


</div>

</div>


### mixedLambdaModifiers

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">inline </span><span class="kd">fun </span><a href="index.html#mixedlambdamodifiers"><span class="nf">mixedLambdaModifiers</span></a><span class="p">(</span>
    <span class="kd">crossinline </span>required<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">, </span>
    <span class="kd">noinline </span>optional<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="o"> = </span>{}<span class="p">, </span>
    fallback<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="o"> = </span>{}
<span class="p">)</span>
</span></code></pre></div>
</div>


<div markdown>

An inline function with mixed lambda modifiers.


</div>

</div>


### mixedParameters

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#mixedparameters"><span class="nf">mixedParameters</span></a><span class="p">(</span>
    name<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">, </span>
    count<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html">Int</a><span class="o"> = </span><span class="nb">10</span><span class="p">, </span>
    <span class="kd">vararg </span>tags<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">, </span>
    onComplete<span class="o">: </span><span class="p">(</span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a>
<span class="p">)</span>
</span></code></pre></div>
</div>


<div markdown>

A function with mixed parameter types.


</div>

</div>


### multipleLambdas

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#multiplelambdas"><span class="nf">multipleLambdas</span></a><span class="p">(</span>onSuccess<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">, </span>onError<span class="o">: </span><span class="p">(</span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-throwable/index.html">Throwable</a><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">)</span>
</span></code></pre></div>
</div>


<div markdown>

A function with multiple lambda parameters.


</div>

</div>


### multipleReified

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">inline </span><span class="kd">fun </span><span class="o"><</span><a href="index.html#multiplereified">T</a><span class="p">, </span><a href="index.html#multiplereified">U</a><span class="o">> </span><a href="index.html#multiplereified"><span class="nf">multipleReified</span></a><span class="p">(</span>first<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-any/index.html">Any</a><span class="p">, </span>second<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-any/index.html">Any</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html">Boolean</a>
</span></code></pre></div>
</div>


<div markdown>

An inline function with multiple reified type parameters.


</div>

</div>


### noinlineLambda

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">inline </span><span class="kd">fun </span><a href="index.html#noinlinelambda"><span class="nf">noinlineLambda</span></a><span class="p">(</span>onComplete<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">, </span><span class="kd">noinline </span>onCancel<span class="o">: </span><span class="p">(</span><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">)</span>
</span></code></pre></div>
</div>


<div markdown>

An inline function with noinline lambda parameter.


</div>

</div>


### normalParameters

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#normalparameters"><span class="nf">normalParameters</span></a><span class="p">(</span>name<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">, </span>age<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html">Int</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a>
</span></code></pre></div>
</div>


<div markdown>

A function with normal parameters.


</div>

</div>


### reifiedTypeParameter

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">inline </span><span class="kd">fun </span><span class="o"><</span><a href="index.html#reifiedtypeparameter">T</a><span class="o">> </span><a href="index.html#reifiedtypeparameter"><span class="nf">reifiedTypeParameter</span></a><span class="p">(</span>value<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-any/index.html">Any</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html">Boolean</a>
</span></code></pre></div>
</div>


<div markdown>

An inline function with reified type parameter.


</div>

</div>


### varargParameters

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#varargparameters"><span class="nf">varargParameters</span></a><span class="p">(</span><span class="kd">vararg </span>items<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html">String</a>
</span></code></pre></div>
</div>


<div markdown>

A function with a vararg parameter.


</div>

</div>


### varargWithLambda

<div class="dokka-symbol-block" markdown>

<div markdown>

<div class="highlight"><pre><code class="md-code__content"><span markdown><span class="kd">fun </span><a href="index.html#varargwithlambda"><span class="nf">varargWithLambda</span></a><span class="p">(</span><span class="kd">vararg </span>items<span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html">Int</a><span class="p">, </span>action<span class="o">: </span><span class="p">(</span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html">Int</a><span class="p">)</span><span class="o"> -> </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html">Unit</a><span class="p">)</span><span class="o">: </span><a href="https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-nothing/index.html">Nothing</a>
</span></code></pre></div>
</div>


<div markdown>

A function with vararg and a trailing lambda.


</div>

</div>

