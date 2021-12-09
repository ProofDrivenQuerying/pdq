const prodAPI = 'http://pdq-webapp.cs.ox.ac.uk/pdq-react-rest';
const localAPI = 'http://localhost:8080';
// the one that is actually going to be used
const api = process.env.NODE_ENV === `development` ? localAPI : prodAPI;

export { prodAPI, localAPI, api };
