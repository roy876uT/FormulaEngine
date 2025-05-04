# Overview
Java BigDecimal is famous in achieving high accuracy in financial calculation. However, it comes with some common pitfalls that develpers need to know before it can reliably run in production. The aim of Formula engine and Mutable Decimal in this project is to fix the problems and provide handy tool for financial computation.     
Reference: https://blogs.oracle.com/javamagazine/post/four-common-pitfalls-of-the-bigdecimal-class-and-how-to-avoid-them
# Formula Engine
The formula that coded in BigDecimal expressed in an object oriented way. For example, we can express this calculation (100 + 950.45) * 12 / 100 in BigDecimal. It becomes,  
`(BigDecimal("100").add(BigDecimal("950.45))).multiply(BigDecimal("12")).divide(BigDecimal("100))`  
The formula maintained in Java source. Developer's effort is needed for every formula update. To streamline the update process, formula engine come to rescue in this case. It take formula input as string and allow formula stored in external config. In case of requirement change, formula update can be done by devop engineer or end-user.
The formula in above example could become, `(100 + 950.45) * 12 / 100`. It is readable and allow data update to be done by staff from all level.
Formula engine is implemented by BigDecimal and MutableDecimal under the hood such that the accuracy is guaranteed.

## Mutable Decimal
### Goal  
- fix BigDecimal's compare & equal inconsistency. For example, Java MutableDecimal Set's content is consistent across different implementation, eg, HashSet & TreeSet
- encourage object reuse to reduce GC collection
- check equality by numeral value despite different scale, eg, 0.5 equals 0.500  
### Note
- this implementation is not synchronized. External synchronization is recommended in multi-thread environment 
