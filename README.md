# About the App

## Architecture
- MVVM + Clean Architecture
  - Separate modules for core ui layer, domain layer and data layer 
- Pagination with Paging 3
- Multi-module gradle projects for scalability
- Created base modules for ui layer, domain layer and data layer

## Major Libraries
- UI: XMLs
- DI: Hilt
- Image Loading: Glide
- Network: Okhttp and Retrofit
- Parsing: Moshi with kapt

## Unit Testing
- Mockk for testing of ViewModel, Domain layer and Data layer
- Mock Web server for Api Testing

## Edge cases handled
- Process Death
- UI will show error states in form of toasts and Textview
  - TextView error will only be shown when RecyclerView is empty
- ViewModel is exposing SharedFlow to render Paging Data
  - It has replay of 1 to handle edge case like when user goes away from HomeActivity while we are still waiting for results
- network error while scrolling. We will still retry automatically when the user will scroll to last row(the row number can be configured)
  - Code is present in: com.home.presentation.activities.HomeActivity scrollListener

## Gradle
- Created different gradle files to reduce redundant code
- Its present in buildconfig folder 

## Caching
- This app is relying on HTTP Caching
- Local DB caching is not used because it wasn't mentioned in project todos

## UX Work
- Pagination
- Handle Error States
- Handled Process Death
- User can initiate search from keyboard
- If user has entered new query then search results will appear from top
- If user presses search button again then PagingAdapter.refresh call will invoke 

## Screenshots

<img src="https://github.com/rahul-lohra/Github-Demo-App/blob/master/screenshots/initial_screen.PNG?raw=true" align="left" width="240" >
<img src="https://github.com/rahul-lohra/Github-Demo-App/blob/master/screenshots/loading_screen.PNG?raw=true" align="left" width="240" >
<img src="https://github.com/rahul-lohra/Github-Demo-App/blob/master/screenshots/success_state.PNG?raw=true" align="left" width="240" >
<img src="https://github.com/rahul-lohra/Github-Demo-App/blob/master/screenshots/error_state.PNG?raw=true" align="left" width="240" >
<img src="https://github.com/rahul-lohra/Github-Demo-App/blob/master/screenshots/network_error_state.PNG?raw=true" width="240" >

## Things I did not do
- Testing
  - Test init block of com.home.presentation.viewmodels.HomeViewModel
  - Reason: Cannot find any resources on how to do it with mockk
- In case of Process Death I am only fetching 1 page instead of all visited pages.
  - Reason: Running out of time
