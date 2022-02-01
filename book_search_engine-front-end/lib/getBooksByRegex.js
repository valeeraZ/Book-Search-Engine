export default function getBooksByRegex(regex, closeness) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?regex=${regex}&closeness=${closeness}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
