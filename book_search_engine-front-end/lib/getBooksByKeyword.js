export default function getBooksByKeyword(keyword, closeness) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?search=${keyword}&closeness=${closeness}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
