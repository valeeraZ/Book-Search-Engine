export default function getBooksBySuggestions(suggestions) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?suggestions=${suggestions}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
