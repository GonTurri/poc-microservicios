import React, { createContext, useState, useContext, useCallback, useEffect } from 'react';
import apiClient from '../services/api';
import { useAuth0 } from '@auth0/auth0-react'; // Import useAuth0

const CartContext = createContext();

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
};

export const CartProvider = ({ children }) => {
    // We only need state for the cart itself and loading status
    const [cart, setCart] = useState(null); // Store the whole cart object { items: [...] }
    const [isLoadingCart, setIsLoadingCart] = useState(true);
    const { isAuthenticated, isLoading: isAuthLoading } = useAuth0(); // Get Auth0 state

    // Fetch cart data from the API
    const fetchCart = useCallback(async () => {
        // Only proceed if authenticated and auth isn't loading
        if (!isAuthLoading && isAuthenticated) {
            setIsLoadingCart(true);
            try {
                console.log('Fetching cart...'); // Log
                const response = await apiClient.get('/carts');
                setCart(response.data || { items: [] }); // Set cart or empty cart
                console.log('Cart fetched:', response.data); // Log
            } catch (error) {
                console.error('Failed to fetch cart:', error);
                setCart({ items: [] }); // Reset cart on error
            } finally {
                setIsLoadingCart(false);
            }
        } else if (!isAuthLoading && !isAuthenticated) {
            // If logged out and auth is ready, ensure cart is null/empty and not loading
            setCart(null);
            setIsLoadingCart(false);
        }
        // If isAuthLoading is true, do nothing yet, wait for auth state
    }, [isAuthenticated, isAuthLoading]); // Depend on auth state

    // Effect to run fetchCart when auth state stabilizes
    useEffect(() => {
        fetchCart();
    }, [fetchCart]); // fetchCart is stable due to useCallback dependencies

    // Function to add item
    const addToCart = useCallback(async (productId, amount = 1) => {
        if (!isAuthenticated) return false;
        try {
            const response = await apiClient.post('/carts/items', { productId, amount });
            setCart(response.data); // Update state directly from response
            return true;
        } catch (error) {
            console.error('Error adding to cart:', error);
            throw error;
        }
    }, [isAuthenticated]);

    // Function to update item
    const updateCartItem = useCallback(async (productId, amount) => {
        if (!isAuthenticated) return false;
        try {
            const response = await apiClient.patch(`/carts/items/${productId}`, { amount });
            setCart(response.data); // Update state directly from response
        } catch (error) {
            console.error('Error updating cart item:', error);
            throw error;
        }
    }, [isAuthenticated]);

    // Function to remove item
    const removeFromCart = useCallback(async (productId) => {
        if (!isAuthenticated) return false;
        try {
            const response = await apiClient.delete(`/carts/items/${productId}`);
            setCart(response.data); // Update state directly from response
        } catch (error) {
            console.error('Error removing from cart:', error);
            throw error;
        }
    }, [isAuthenticated]);

    // Function for checkout
    const checkout = useCallback(async () => {
        if (!isAuthenticated) {
            // Throw error if not authenticated, will be caught by toast.promise
            throw new Error("User not authenticated.");
        }

        try {
            const response = await apiClient.post('/orders');

            // Check if the response is the fallback DTO
            if (response.data && response.data.description && !response.data.id) {
                console.warn("Checkout fallback response received:", response.data.description);
                throw new Error(response.data.description);
            }
            // Check if it's a successful order
            else if (response.data && response.data.id) {
                await fetchCart();
                console.log("hola estoy aca " + cart)
                return response.data;
            }
            else {
                console.error("Unexpected checkout response:", response.data);
                throw new Error("An unexpected error occurred during checkout.");
            }
        } catch (error) {
            console.error('Error during checkout API call:', error);
            throw error;
        }
    }, [isAuthenticated, fetchCart]); // Depend on fetchCart

    // Calculate cart count directly from the cart state
    const cartCount = cart?.items ? cart.items.reduce((count, item) => count + item.amount, 0) : 0;

    // Value provided to consuming components
    const value = {
        cart,
        cartCount, // Use the derived count
        isLoading: isLoadingCart, // Use the specific loading state
        fetchCart, // Expose fetchCart if manual refresh is needed
        addToCart,
        updateCartItem,
        removeFromCart,
        checkout
    };

    return (
        <CartContext.Provider value={value}>
            {children}
        </CartContext.Provider>
    );
};