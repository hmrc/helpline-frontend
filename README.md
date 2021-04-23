
# helpline-frontend

helpline-frontend is a service that will display contact pages on demand.

### Running
To run the application, use `sbt run`. 

## API

| Method | Path                                        | Description                                      |
|--------|---------------------------------------------|--------------------------------------------------|
| GET    | /helpline/:helpKey?back=backURL             | display contact pages on demand                  |

Eg./helpline/deceased?back=backURL (the helpKey is case-insensitive)

We permit one optional query parameter, ?back=URL. When this is set by the calling service it will be used to create a visible back button on the contact page.

Several alternatives will be available and can be selected by indicating the helpKey that represents the particular helpdesk using the URL.

| helpKey              | Page                                         |
|----------------------|----------------------------------------------|
| CHILDBENEFIT         | details about CHILDBENEFIT information       |
| INCOMETAX            | details about INCOMETAX information          |
| NATIONALINSURANCE    | details about NATIONALINSURANCE information  |
| PAYEFOREMPLOYERS     | details about PAYEFOREMPLOYERS information   |
| DECEASED             | details about DECEASED information           |
| SELFASSESSMENT       | details about SELFASSESSMENT information     |
| STATEPENSION         | details about STATEPENSION information       |
| TAXCREDITS           | details about TAXCREDITS information         |
| anything else        | DEFAULT page                                 |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
