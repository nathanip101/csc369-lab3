Nathan Ip

I decided to do map-side joins for all of these as it seemed more efficient. In
general, map-side joins are useful for when one file is significantly smaller 
than the other. This is true in this case. The hostname_country.csv file is 
smaller than the log files. It also seemed more straightforward to convert 
mappings from hostnames to country as soon as possible in the map/reduce stage 
as there are more hostnames than countries. So by mapping hostnames to country, 
we can reduce the number of different keys. 

CountryRequestCount:
    Map:
        Input: Apache HTTP file
        Output: (k, v) where k is the country and v is 1
    Reduce:
        Input: (k, v) where k is the country and v is 1
        output: (k, v) where k is the country and v is the number of request 
        made to hostnames from that country
CountryURLRequestCount:
    Map: 
        Input: Apache HTTP log file
        Output: (k, v) where k is the country concatenated with the url and v is 1
    Reduce: 
        Input: (k, v) where k is the country concatenated with the url and v is 1
        Output: (k, v) where k is the country and v is the url concatenated with
        the request counts. 
    Map2: output of reduce. 
    Reduce2: (k, v) where k is the country + url and v is the number of request
    made to that url. 
CountryVisitorsCount:
    Map: 
        Input: Apache HTTP log file
        Output: (k, v) where k is a url and v is a country that has visited that
        url. 
    Reduce: 
        Input: (k, v) where k is a url and v is a country that has visited that
        url. 
        Output: (k, v) where k is a url and v is a list of countries that has
        visited that url. 

