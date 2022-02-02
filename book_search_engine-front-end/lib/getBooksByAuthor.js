export default function getBooksByAuthor(keywordOrRegex, closeness) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?searchByAuthor=${keywordOrRegex}&closeness=${closeness}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
