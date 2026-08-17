document.addEventListener('DOMContentLoaded', () => {
    const shortenForm = document.getElementById('shortenForm');
    const originalUrlInput = document.getElementById('originalUrl');
    const resultContainer = document.getElementById('resultContainer');
    const shortUrlOutput = document.getElementById('shortUrlOutput');
    const copyBtn = document.getElementById('copyBtn');
    const copyBtnText = document.getElementById('copyBtnText');

    const statsForm = document.getElementById('statsForm');
    const shortCodeInput = document.getElementById('shortCodeInput');
    const statsResult = document.getElementById('statsResult');

    const statShortCode = document.getElementById('statShortCode');
    const statClickCount = document.getElementById('statClickCount');
    const statOriginalUrl = document.getElementById('statOriginalUrl');
    const statCreatedAt = document.getElementById('statCreatedAt');
    const statLastAccessed = document.getElementById('statLastAccessed');

    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');

    shortenForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const originalUrl = originalUrlInput.value.trim();

        if (!originalUrl) return;

        try {
            const response = await fetch('/api/urls', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ originalUrl })
            });

            const data = await response.json();

            if (!response.ok) {
                showToast(data.error || 'Failed to shorten URL', true);
                return;
            }

            shortUrlOutput.value = data.shortUrl;
            resultContainer.classList.remove('hidden');
            showToast('URL shortened successfully!', false);
        } catch (error) {
            showToast('Network error. Please try again.', true);
        }
    });

    copyBtn.addEventListener('click', async () => {
        if (!shortUrlOutput.value) return;

        try {
            await navigator.clipboard.writeText(shortUrlOutput.value);
            copyBtnText.textContent = 'Copied!';
            showToast('Copied to clipboard!', false);

            setTimeout(() => {
                copyBtnText.textContent = 'Copy';
            }, 2000);
        } catch (err) {
            showToast('Failed to copy to clipboard', true);
        }
    });

    statsForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        let shortCode = shortCodeInput.value.trim();

        if (shortCode.includes('/')) {
            const parts = shortCode.split('/');
            shortCode = parts[parts.length - 1];
        }

        if (!shortCode) return;

        try {
            const response = await fetch(`/api/urls/${encodeURIComponent(shortCode)}/stats`);
            const data = await response.json();

            if (!response.ok) {
                statsResult.classList.add('hidden');
                showToast(data.error || 'Short code not found', true);
                return;
            }

            statShortCode.textContent = data.shortCode;
            statClickCount.textContent = data.clickCount;
            statOriginalUrl.textContent = data.originalUrl;
            statOriginalUrl.href = data.originalUrl;

            statCreatedAt.textContent = formatDate(data.createdAt);
            statLastAccessed.textContent = data.lastAccessedAt ? formatDate(data.lastAccessedAt) : 'Never';

            statsResult.classList.remove('hidden');
        } catch (error) {
            showToast('Error fetching statistics', true);
        }
    });

    function formatDate(isoString) {
        if (!isoString) return '-';
        try {
            const date = new Date(isoString);
            return date.toLocaleString();
        } catch (e) {
            return isoString;
        }
    }

    function showToast(message, isError = false) {
        toastMessage.textContent = message;
        toast.className = `toast ${isError ? 'error' : 'success'}`;
        toast.classList.remove('hidden');

        setTimeout(() => {
            toast.classList.add('hidden');
        }, 3500);
    }
});
