import App from './App';

// #ifdef VUE3
import { createSSRApp } from 'vue';

// #ifdef H5
const H5_LAST_URL_KEY = '__uni_h5_last_url__';
const H5_PENDING_RELOAD_KEY = '__uni_h5_pending_reload__';
let h5ReloadScheduled = false;
let h5ImageObserverInitialized = false;

function persistH5Location() {
    try {
        sessionStorage.setItem(H5_LAST_URL_KEY, window.location.href);
    } catch (error) {
        console.warn('persistH5Location failed', error);
    }
}

function restoreH5LocationIfNeeded() {
    try {
        const pendingReload = sessionStorage.getItem(H5_PENDING_RELOAD_KEY);
        const lastUrl = sessionStorage.getItem(H5_LAST_URL_KEY);
        if (pendingReload === '1' && lastUrl && window.location.href !== lastUrl) {
            window.location.replace(lastUrl);
            return true;
        }
        sessionStorage.removeItem(H5_PENDING_RELOAD_KEY);
    } catch (error) {
        console.warn('restoreH5LocationIfNeeded failed', error);
    }
    return false;
}

function shouldRecoverFromErrorMessage(message) {
    if (!message) {
        return false;
    }
    return [
        'Failed to fetch dynamically imported module',
        'Importing a module script failed',
        'does not provide an export named',
        'Unhandled error during execution of async component loader'
    ].some((keyword) => message.includes(keyword));
}

function scheduleH5Reload(reason) {
    if (h5ReloadScheduled) {
        return;
    }
    h5ReloadScheduled = true;
    persistH5Location();
    try {
        sessionStorage.setItem(H5_PENDING_RELOAD_KEY, '1');
    } catch (error) {
        console.warn('scheduleH5Reload failed to mark pending reload', error);
    }
    console.warn(`[H5 Recovery] ${reason}, reloading current route...`);
    setTimeout(() => {
        window.location.reload();
    }, 60);
}

function optimizeH5Images(root = document) {
    const images = root.querySelectorAll ? root.querySelectorAll('img') : [];
    images.forEach((img) => {
        if (img.dataset.codexOptimized === '1') {
            return;
        }
        img.decoding = 'async';
        const top = img.getBoundingClientRect ? img.getBoundingClientRect().top : 0;
        const isAboveFold = top > -120 && top < window.innerHeight * 1.2;
        img.loading = isAboveFold ? 'eager' : 'lazy';
        img.setAttribute('fetchpriority', isAboveFold ? 'high' : 'low');
        img.dataset.codexOptimized = '1';
    });
}

function setupH5ImageOptimization() {
    if (h5ImageObserverInitialized) {
        return;
    }
    h5ImageObserverInitialized = true;

    const runOptimizeImages = () => {
        requestAnimationFrame(() => {
            optimizeH5Images(document);
        });
    };

    runOptimizeImages();
    window.addEventListener('load', runOptimizeImages);
    window.addEventListener('scroll', runOptimizeImages, { passive: true });
    window.addEventListener('resize', runOptimizeImages, { passive: true });

    const observer = new MutationObserver(() => {
        runOptimizeImages();
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}

function setupH5RuntimeRecovery(app) {
    restoreH5LocationIfNeeded();
    persistH5Location();
    setupH5ImageOptimization();

    window.addEventListener('hashchange', persistH5Location);
    window.addEventListener('beforeunload', persistH5Location);

    window.addEventListener('error', (event) => {
        const message = event?.message || '';
        if (shouldRecoverFromErrorMessage(message)) {
            scheduleH5Reload(message);
        }
    });

    window.addEventListener('unhandledrejection', (event) => {
        const reason = event?.reason;
        const message = typeof reason === 'string' ? reason : reason?.message || '';
        if (shouldRecoverFromErrorMessage(message)) {
            scheduleH5Reload(message);
        }
    });

    app.config.errorHandler = (error, instance, info) => {
        console.error('[Vue Error Handler]', info, error, instance);
        const message = error?.message || String(error || '');
        if (shouldRecoverFromErrorMessage(message)) {
            scheduleH5Reload(message);
        }
    };

    if (import.meta.hot) {
        import.meta.hot.on('vite:beforeUpdate', () => {
            persistH5Location();
            try {
                sessionStorage.setItem(H5_PENDING_RELOAD_KEY, '1');
            } catch (error) {
                console.warn('failed to set HMR pending reload flag', error);
            }
        });

        import.meta.hot.on('vite:afterUpdate', () => {
            try {
                if (sessionStorage.getItem(H5_PENDING_RELOAD_KEY) === '1') {
                    scheduleH5Reload('vite module update');
                }
            } catch (error) {
                console.warn('failed to read HMR pending reload flag', error);
            }
        });

        import.meta.hot.on('vite:beforeFullReload', () => {
            persistH5Location();
            try {
                sessionStorage.setItem(H5_PENDING_RELOAD_KEY, '1');
            } catch (error) {
                console.warn('failed to mark full reload flag', error);
            }
        });
    }
}
// #endif

export function createApp() {
    const app = createSSRApp(App);
    // #ifdef H5
    setupH5RuntimeRecovery(app);
    // #endif
    return {
        app
    };
}
// #endif
