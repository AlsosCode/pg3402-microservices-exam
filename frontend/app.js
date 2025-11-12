// API Configuration - Using API Gateway as single entry point
// Check if running in production (Vercel) or locally
const isProduction = window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1';
const API_GATEWAY_URL = isProduction
    ? 'https://your-api-gateway.onrender.com'  // Replace with your Render URL
    : 'http://localhost:8080';
const API_BASE_URL = `${API_GATEWAY_URL}/api/catalog`;
const COLLECTION_API_URL = `${API_GATEWAY_URL}/api/collections`;
const MEDIA_BASE_URL = isProduction
    ? 'https://your-media-service.onrender.com'  // Replace with your Render URL
    : 'http://localhost:8084';
const SET_CODE = 'SV01';
const USER_ID = 1; // Default user

// State
let allCards = [];
let collectionCards = [];
let currentView = 'catalog';
let selectedCard = null;

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    initializeTheme();
    initializeNavigation();
    initializeSearchAndFilters();
    initializeModal();
    loadCatalog();
    loadCollection();
});

// Theme Toggle
function initializeTheme() {
    const themeToggle = document.getElementById('theme-toggle');
    const icon = themeToggle.querySelector('i');

    // Check for saved theme preference or default to light mode
    const currentTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', currentTheme);
    updateThemeIcon(icon, currentTheme);

    themeToggle.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateThemeIcon(icon, newTheme);
    });
}

function updateThemeIcon(icon, theme) {
    if (theme === 'dark') {
        icon.classList.remove('fa-moon');
        icon.classList.add('fa-sun');
    } else {
        icon.classList.remove('fa-sun');
        icon.classList.add('fa-moon');
    }
}

// Navigation
function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const view = item.dataset.view;
            switchView(view);

            // Update active state
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

function switchView(view) {
    currentView = view;

    // Hide all views
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));

    // Show selected view
    const viewElement = document.getElementById(`${view}-view`);
    if (viewElement) {
        viewElement.classList.add('active');
    }

    // Load data if needed
    if (view === 'collection') {
        loadCollection();
    } else if (view === 'progress') {
        loadProgress();
    }
}

// Search and Filters
function initializeSearchAndFilters() {
    const searchInput = document.getElementById('search-input');
    const rarityFilter = document.getElementById('rarity-filter');

    searchInput.addEventListener('input', debounce(() => {
        filterCards();
    }, 300));

    rarityFilter.addEventListener('change', () => {
        filterCards();
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function filterCards() {
    const searchTerm = document.getElementById('search-input').value.toLowerCase();
    const rarity = document.getElementById('rarity-filter').value;

    let filtered = allCards;

    if (searchTerm) {
        filtered = filtered.filter(card =>
            card.name.toLowerCase().includes(searchTerm) ||
            card.cardNumber.includes(searchTerm)
        );
    }

    if (rarity) {
        filtered = filtered.filter(card => card.rarity === rarity);
    }

    displayCards(filtered);
    document.getElementById('showing-cards').textContent = filtered.length;
}

// Load Catalog
async function loadCatalog() {
    showLoading(true);

    try {
        const response = await fetch(`${API_BASE_URL}/sets/${SET_CODE}/cards`);
        if (!response.ok) throw new Error('Failed to fetch cards');

        allCards = await response.json();
        displayCards(allCards);

        document.getElementById('total-cards').textContent = allCards.length;
        document.getElementById('showing-cards').textContent = allCards.length;

    } catch (error) {
        console.error('Error loading catalog:', error);
        showError('Failed to load cards. Please try again.');
    } finally {
        showLoading(false);
    }
}

function displayCards(cards) {
    const grid = document.getElementById('cards-grid');
    grid.innerHTML = '';

    if (cards.length === 0) {
        grid.innerHTML = '<div class="no-cards">No cards found</div>';
        return;
    }

    cards.forEach(card => {
        const cardElement = createCardElement(card);
        grid.appendChild(cardElement);
    });
}

function createCardElement(card) {
    const div = document.createElement('div');
    div.className = 'card-item';

    // Check if card is in collection
    const isInCollection = collectionCards.some(c => c.cardId === card.id);
    if (isInCollection) {
        div.classList.add('in-collection');
    }

    // Convert image URL to use localhost instead of media-service container name
    const imageUrl = card.imageUrl.replace('http://media-service:8084', MEDIA_BASE_URL);

    div.innerHTML = `
        <div class="card-image">
            <img src="${imageUrl}" alt="${card.name}" loading="lazy">
            ${isInCollection ? '<div class="collection-badge">✓</div>' : ''}
        </div>
        <div class="card-info">
            <div class="card-number">#${card.cardNumber}</div>
            <div class="card-name">${card.name}</div>
            <div class="card-rarity ${card.rarity.toLowerCase()}">${formatRarity(card.rarity)}</div>
        </div>
    `;

    div.addEventListener('click', () => showCardDetail(card));

    return div;
}

function formatRarity(rarity) {
    return rarity.replace(/_/g, ' ').toLowerCase()
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
}

// Load Collection
async function loadCollection() {
    try {
        const response = await fetch(`${COLLECTION_API_URL}/users/${USER_ID}/cards`);
        if (!response.ok) throw new Error('Failed to fetch collection');

        collectionCards = await response.json();
        displayCollection();
        updateCollectionStats();

    } catch (error) {
        console.error('Error loading collection:', error);
        // If collection is empty or service is down, show empty state
        collectionCards = [];
        displayCollection();
    }
}

function displayCollection() {
    const grid = document.getElementById('collection-grid');
    grid.innerHTML = '';

    if (collectionCards.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <svg width="64" height="64" viewBox="0 0 20 20" fill="currentColor" opacity="0.3">
                    <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"/>
                    <path fill-rule="evenodd" d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z" clip-rule="evenodd"/>
                </svg>
                <p>Your collection is empty</p>
                <p class="empty-hint">Add cards from the catalog to start your collection</p>
            </div>
        `;
        return;
    }

    // Find the actual card data for each collection card
    collectionCards.forEach(collectionCard => {
        const card = allCards.find(c => c.id === collectionCard.cardId);
        if (card) {
            const cardElement = createCollectionCardElement(card, collectionCard);
            grid.appendChild(cardElement);
        }
    });
}

function createCollectionCardElement(card, collectionCard) {
    const div = document.createElement('div');
    div.className = 'card-item collection-card';

    const imageUrl = card.imageUrl.replace('http://media-service:8084', MEDIA_BASE_URL);

    div.innerHTML = `
        <div class="card-image">
            <img src="${imageUrl}" alt="${card.name}" loading="lazy">
            <div class="quantity-badge">×${collectionCard.quantity}</div>
        </div>
        <div class="card-info">
            <div class="card-number">#${card.cardNumber}</div>
            <div class="card-name">${card.name}</div>
            <div class="card-condition">${formatCondition(collectionCard.condition)}</div>
        </div>
    `;

    div.addEventListener('click', () => showCardDetail(card));

    return div;
}

function formatCondition(condition) {
    return condition.replace(/_/g, ' ').toLowerCase()
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
}

function updateCollectionStats() {
    const uniqueCount = collectionCards.length;
    const totalCount = collectionCards.reduce((sum, card) => sum + card.quantity, 0);

    document.getElementById('unique-owned').textContent = uniqueCount;
    document.getElementById('total-owned').textContent = totalCount;
}

// Load Progress
async function loadProgress() {
    try {
        const response = await fetch(`${COLLECTION_API_URL}/users/${USER_ID}/card-ids`);
        if (!response.ok) throw new Error('Failed to fetch progress');

        const ownedCardIds = await response.json();

        // Map card IDs to card numbers
        const ownedCards = allCards.filter(card => ownedCardIds.includes(card.id));
        const ownedNumbers = ownedCards.map(card => parseInt(card.cardNumber));

        // Calculate progress for different set types
        const standardCount = ownedNumbers.filter(num => num >= 1 && num <= 198).length;
        const fullCount = ownedNumbers.filter(num => num >= 1 && num <= 258).length;

        // Standard Set (1-198)
        updateProgressBar('standard', standardCount, 198);
        document.getElementById('standard-count').textContent = standardCount;

        // Full Set (1-258)
        updateProgressBar('full', fullCount, 258);
        document.getElementById('full-count').textContent = fullCount;

        // Complete Set (with reverse holos - 516 total)
        // For now, we'll use the same count since we don't track reverse holos separately yet
        updateProgressBar('complete', fullCount, 516);
        document.getElementById('complete-count').textContent = fullCount;

    } catch (error) {
        console.error('Error loading progress:', error);
    }
}

function updateProgressBar(id, current, total) {
    const percentage = (current / total) * 100;
    const progressBar = document.getElementById(`progress-${id}`);
    if (progressBar) {
        progressBar.style.width = `${percentage}%`;
    }
}

// Card Detail Modal
function initializeModal() {
    const modal = document.getElementById('card-modal');
    const closeBtn = document.querySelector('.modal-close');
    const addBtn = document.getElementById('add-to-collection');
    const deleteBtn = document.getElementById('delete-from-collection');

    closeBtn.addEventListener('click', () => {
        modal.classList.remove('active');
    });

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('active');
        }
    });

    addBtn.addEventListener('click', () => {
        if (selectedCard) {
            addToCollection(selectedCard);
        }
    });

    deleteBtn.addEventListener('click', () => {
        if (selectedCard) {
            deleteFromCollection(selectedCard);
        }
    });
}

function showCardDetail(card) {
    selectedCard = card;
    const modal = document.getElementById('card-modal');

    const imageUrl = card.imageUrl.replace('http://media-service:8084', MEDIA_BASE_URL);

    document.getElementById('modal-card-image').src = imageUrl;
    document.getElementById('modal-card-name').textContent = card.name;
    document.getElementById('modal-card-number').textContent = `#${card.cardNumber}`;
    document.getElementById('modal-card-set').textContent = card.setName;
    document.getElementById('modal-card-rarity').textContent = formatRarity(card.rarity);
    document.getElementById('modal-card-type').textContent = card.type || 'N/A';
    document.getElementById('modal-card-artist').textContent = card.artist || 'Unknown';

    // Update button state
    const isInCollection = collectionCards.some(c => c.cardId === card.id);
    const addBtn = document.getElementById('add-to-collection');
    const deleteBtn = document.getElementById('delete-from-collection');

    if (isInCollection) {
        // Show delete button, hide add button
        addBtn.style.display = 'none';
        deleteBtn.style.display = 'flex';
        deleteBtn.disabled = false;
    } else {
        // Show add button, hide delete button
        addBtn.style.display = 'flex';
        deleteBtn.style.display = 'none';
        addBtn.innerHTML = `
            <i class="fa-solid fa-plus"></i>
            Add to Collection
        `;
        addBtn.disabled = false;
    }

    modal.classList.add('active');
}

// Add to Collection
async function addToCollection(card) {
    try {
        const response = await fetch(`${COLLECTION_API_URL}/users/${USER_ID}/cards`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                cardId: card.id,
                quantity: 1,
                condition: 'NEAR_MINT',
                isReverseHolo: false
            })
        });

        if (!response.ok) {
            throw new Error('Failed to add card to collection');
        }

        // Reload collection and update UI
        await loadCollection();

        // Update modal button
        const addBtn = document.getElementById('add-to-collection');
        addBtn.textContent = 'Added to Collection!';
        addBtn.disabled = true;

        // Refresh catalog view to show collection badge
        if (currentView === 'catalog') {
            filterCards();
        }

        showNotification('Card added to collection!');

    } catch (error) {
        console.error('Error adding to collection:', error);
        showNotification('Failed to add card. Please try again.', 'error');
    }
}

// Delete from Collection
async function deleteFromCollection(card) {
    try {
        const response = await fetch(`${COLLECTION_API_URL}/users/${USER_ID}/cards/${card.id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Failed to delete card from collection');
        }

        // Reload collection and update UI
        await loadCollection();

        // Close modal
        document.getElementById('card-modal').classList.remove('active');

        // Refresh catalog view to remove collection badge
        if (currentView === 'catalog') {
            filterCards();
        }

        showNotification('Card removed from collection!');

    } catch (error) {
        console.error('Error deleting from collection:', error);
        showNotification('Failed to remove card. Please try again.', 'error');
    }
}

// Utility Functions
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = show ? 'flex' : 'none';
    }
}

function showError(message) {
    const grid = document.getElementById('cards-grid');
    grid.innerHTML = `<div class="error-message">${message}</div>`;
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}
