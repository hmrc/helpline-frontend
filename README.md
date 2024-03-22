
# helpline-frontend

helpline-frontend is a service that will display contact pages on demand.

### Running
To run the application, use `sbt run`.

When running the integration tests you can start the DATASTREAM service
with the command

`sm2 --start DATASTREAM`

to suppress errors caused by calls to auditing. However, the tests will run successfully
without the support of the data stream service.

## API

| Method | Path                                             | Description                                      |
|--------|--------------------------------------------------|--------------------------------------------------|
| GET    | /helpline/call-options-no-answers                | display the list of contact pages selectable     |
| GET    | /helpline/which-service-are-you-trying-to-access | contact pages for standalone IV journey          |
| GET    | /helpline/:helpKey?back=backURL                  | display contact pages on demand                  |

Eg./helpline/died?back=backURL (the helpKey is case-insensitive)

We permit one optional query parameter, ?back=URL. When this is set by the calling service it will be used to create a visible back button on the contact page.

Several alternatives will be available and can be selected by indicating the helpKey that represents the particular helpdesk using the URL.

| helpKey            | Page                                          |
|--------------------|-----------------------------------------------|
| CHILD-BENEFIT      | details about CHILD-BENEFIT information       |
| INCOME-TAX-PAYE    | details about INCOME-TAX-PAYE information     |
| NATIONAL-INSURANCE | details about NATIONAL-INSURANCE information  |
| DIED               | details about DECEASED information            |
| SELF-ASSESSMENT    | details about SELF-ASSESSMENT information     |
| STATE-PENSION      | details about STATE-PENSION information       |
| TAX-CREDITS        | details about TAX-CREDITS information         |
| anything else      | DEFAULT page                                  |


## Find my nino
Please see [How find my nino integrates with Helpline Frontend]("https://github.com/hmrc/verification-diagrams/tree/main/diagrams/find-my-nino-integration")

Please see OriginFilter class for more information on referers

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
