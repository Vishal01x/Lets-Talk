package com.exa.android.letstalk.core.utils

sealed class Response<out T> {
    object Loading : Response<Nothing>()
    data class Success<out T>(val data: T) : Response<T>()
    data class Error(val message: String) : Response<Nothing>()
}


/*
1. Success and Error as data class:
data class is used when you need a class that holds data. It comes with built-in methods like equals(),
hashCode(), toString(), and copy(), which are particularly useful for handling states in your application.
Why Success and Error are data class?

Success holds data (val data: T), so using a data class makes it easy to compare instances, print them
 for debugging, or create modified copies.
Error holds an error message (val message: String), and making it a data class gives the same benefits,
 allowing for better state handling, comparisons, and debugging.

2. Loading as an object:
object is a singleton in Kotlin. This means there is only one instance of it throughout the application.
Why Loading is an object?

Loading represents a simple state that does not need to hold any data. It is enough to know that the
state is Loading, so having a single instance of it (using object) is more efficient than creating multiple instances of a class.
It simplifies the usage since you don’t need to create new instances; you can just refer to Response.Loading.

Summary:
Success and Error as data class: These need to hold data (e.g., the result of an operation or an error message)
 and benefit from data class features like copy(), equals(), etc.
Loading as object: It does not hold data, so using a singleton is sufficient, ensuring minimal memory usage and ease of use.
 */