export default function getBooksByTitle(keywordOrRegex, closeness) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?searchByTitle=${keywordOrRegex}&closeness=${closeness}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
