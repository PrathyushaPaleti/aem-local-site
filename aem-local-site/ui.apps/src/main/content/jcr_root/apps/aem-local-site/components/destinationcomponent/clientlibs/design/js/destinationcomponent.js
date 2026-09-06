// Dynamic relative API endpoint using the Doha (DOH) flight route selector chain [techrevel.blog]
var API_URL = 'http://localhost:4504/content/aem-local-site/us/en/products/product-page/_jcr_content.destinationRoutes.json';

var currentTripType = 'RETURN';
var currentCabinClass = 'ECONOMY';
var flightPricingData = null;

// Layout config matching city codes, image banners and responsive card widths
var destinationMetadata = {
    "CMB": { name: "Colombo", img: "/content/dam/aem-local-site/product-assets/h2-colombo-primary.png", span: "span-2" },
    "AMD": { name: "Ahmedabad", img: "/content/dam/aem-local-site/product-assets/s-ahmedabad-culture.png", span: "span-1" },
    "CDG": { name: "Paris", img: "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80", span: "span-1" },
    "MAN": { name: "Manchester", img: "/content/dam/aem-local-site/product-assets/s-manchester-city2.png", span: "span-1" },
    "ADD": { name: "Addis Ababa", img: "/content/dam/aem-local-site/product-assets/s-addis-ababa-outdoor.png", span: "span-1" },
    "MUC": { name: "Munich", img: "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=800&q=80", span: "span-2" },
    "DUB": { name: "Dublin", img: "/content/dam/aem-local-site/product-assets/h2-dublin.png", span: "span-2" },
    "AMS": { name: "Amsterdam", img: "https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?auto=format&fit=crop&w=800&q=80", span: "span-1" },
    "FRA": { name: "Frankfurt", img: "/content/dam/aem-local-site/product-assets/s-frankfurt-bridge.png", span: "span-1" }
};

document.addEventListener('DOMContentLoaded', () => {
    fetchPricingData();
});

// Retrieves flight fares dynamically from AEM Servvar response [techrevel.blog]
async function fetchPricingData() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error('API fetch failed');
        
        // 1. Get response as raw text first
        const rawText = await response.text();
        console.log("Raw AEM Response: ", rawText); // <--- Inspect this in your browser console!
        
        // 2. Try parsing it manually
        flightPricingData = JSON.parse(rawText);
        filterFares();
    } catch (error) {
        console.error("Flight pricing retrieval error: ", error);
        document.getElementById('faresGrid').innerHTML = `
            <div class="error-message">
                Unable to load flight prices.
            </div>
        `;
    }
}

// Handle dynamic Return vs One-Way tab toggle selection
function setTripType(type) {
    currentTripType = type;
    var btnReturn = document.getElementById('btnReturn');
    var btnOneWay = document.getElementById('btnOneWay');

    if (type === 'RETURN') {
        btnReturn.classList.add('active');
        btnOneWay.classList.remove('active');
    } else {
        btnReturn.classList.remove('active');
        btnOneWay.classList.add('active');
    }
    filterFares();
}

// Date formatter helper (e.g. 2026-10-03 -> 03 Oct 2026)
function formatDate(dateStr) {
    if (!dateStr) return '';
    var options = { day: '2-digit', month: 'short', year: 'numeric' };
    var date = new Date(dateStr);
    return date.toLocaleDateString('en-GB', options);
}

// Processes the dynamic JSON response structure and renders UI elements [techrevel.blog]
function filterFares() {
    if (!flightPricingData || !flightPricingData.pricingResponse) return;

    currentCabinClass = document.getElementById('cabinClassSelect').value;
    var pricing = flightPricingData.pricingResponse[0];
    var currency = pricing.currencyCode;
    var destinationFares = pricing.destinationFares;

    var gridContainer = document.getElementById('faresGrid');
    gridContainer.innerHTML = '';

    // Loop through destinations
    Object.keys(destinationFares).forEach(airportCode => {
        var faresList = destinationFares[airportCode];
        
        // Find flight matching current UI selection parameters
        var matchingFare = faresList.find(fare => 
            fare.cabinClass === currentCabinClass && 
            fare.tripType === currentTripType
        );

        if (matchingFare) {
            var meta = destinationMetadata[airportCode] || { name: airportCode, img: "", span: "span-1" };
            
            var dateDisplay = formatDate(matchingFare.outboundDate);
            if (matchingFare.inboundDate) {
                dateDisplay += ` - ${formatDate(matchingFare.inboundDate)}`;
            }

            // Capitalize Cabin (ECONOMY -> Economy)
            var formattedCabin = matchingFare.cabinClass.charAt(0) + matchingFare.cabinClass.slice(1).toLowerCase();

            // varruct standard HTML Card
            var cardHTML = `
                <a href="${matchingFare.deepLink}" target="_blank" class="card-item ${meta.span}">
                    <img src="${meta.img}" alt="${meta.name}" class="card-img">
                    <div class="card-overlay"></div>
                    <div class="card-content">
                        <h3 class="card-city">${meta.name}</h3>
                        <p class="card-dates">${dateDisplay}</p>
                        <div class="card-footer">
                            <span class="card-cabin">${formattedCabin}</span>
                            <span class="card-price">${currency} ${matchingFare.lowestFare}</span>
                        </div>
                    </div>
                </a>
            `;
            gridContainer.insertAdjacentHTML('beforeend', cardHTML);
        }
    });

    // Handle empty state gracefully
    if (gridContainer.children.length === 0) {
        gridContainer.innerHTML = `<div class="error-message">No fares match your selection at this time.</div>`;
    }
}