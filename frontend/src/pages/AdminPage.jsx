import React, { useState, useEffect, useRef } from 'react';
import apiClient from '../services/api';
import { toast } from 'react-toastify';

const AdminPage = () => {
    const [name, setName] = useState('');
    const [price, setPrice] = useState('');
    const [file, setFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isDragOver, setIsDragOver] = useState(false);
    const fileInputRef = useRef(null);

    const validateAndSetFile = (selectedFile) => {
        if (!selectedFile) {
            setFile(null);
            setPreviewUrl(null);
            return false;
        }

        if (!selectedFile.type.startsWith('image/')) {
            toast.error('Tipo de archivo inválido. Por favor selecciona una imagen.');
            return false;
        }

        if (selectedFile.size > 5 * 1024 * 1024) { // 5MB limit
            toast.error('El archivo es demasiado grande. Máximo 5MB.');
            return false;
        }

        setFile(selectedFile);
        setPreviewUrl(URL.createObjectURL(selectedFile));
        return true;
    };

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        validateAndSetFile(selectedFile);
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setIsDragOver(true);
    };

    const handleDragLeave = (e) => {
        e.preventDefault();
        setIsDragOver(false);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setIsDragOver(false);
        const droppedFile = e.dataTransfer.files[0];
        validateAndSetFile(droppedFile);
    };

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    useEffect(() => {
        return () => {
            if (previewUrl) {
                URL.revokeObjectURL(previewUrl);
            }
        };
    }, [previewUrl]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) {
            toast.error('Please select an image file.');
            return;
        }

        setIsSubmitting(true);

        try {
            const presignedRes = await apiClient.get('/files/presigned-url', {
                params: {
                    fileName: file.name,
                    contentType: file.type
                }
            });

            const { presignedUrl, objectKey } = presignedRes.data;

            const uploadResponse = await fetch(presignedUrl, {
                method: 'PUT',
                body: file,
                headers: {
                    'Content-Type': file.type
                }
            });

            if (!uploadResponse.ok) {
                throw new Error('Falló la subida de la imagen a S3');
            }

            const productData = {
                name: name,
                price: parseFloat(price),
                tempImageKey: objectKey
            };

            await apiClient.post('/products', productData);

            toast.success('¡Producto creado exitosamente!');

            setName('');
            setPrice('');
            setFile(null);
            setPreviewUrl(null);
            e.target.reset();

        } catch (error) {
            console.error("Product creation failed:", error);
            toast.error('Error al crear el producto. Inténtelo más tarde.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 py-8 px-4">
            <div className="max-w-4xl mx-auto">
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold text-gray-900 mb-2">Crear Nuevo Producto</h1>
                    <p className="text-lg text-gray-600">Completa la información para agregar un nuevo producto al catálogo</p>
                </div>

                <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
                    <form onSubmit={handleSubmit} className="p-8 space-y-8">
                        <div className="space-y-6">
                            <div className="flex items-center space-x-3 mb-6">
                                <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                                    <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                </div>
                                <h2 className="text-xl font-semibold text-gray-900">Información del Producto</h2>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-2">
                                    <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                                        Nombre del Producto *
                                    </label>
                                    <div className="relative">
                                        <input
                                            type="text"
                                            id="name"
                                            value={name}
                                            onChange={e => setName(e.target.value)}
                                            className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 disabled:bg-gray-50 disabled:cursor-not-allowed"
                                            placeholder="Ej: iPhone 15 Pro"
                                            required
                                            disabled={isSubmitting}
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <label htmlFor="price" className="block text-sm font-medium text-gray-700">
                                        Precio *
                                    </label>
                                    <div className="relative">
                                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                            <span className="text-gray-500 text-sm font-medium">$</span>
                                        </div>
                                        <input
                                            type="number"
                                            id="price"
                                            value={price}
                                            onChange={e => setPrice(e.target.value)}
                                            step="0.01"
                                            min="0"
                                            className="w-full pl-8 pr-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 disabled:bg-gray-50 disabled:cursor-not-allowed"
                                            placeholder="0.00"
                                            required
                                            disabled={isSubmitting}
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="space-y-6">
                            <div className="flex items-center space-x-3 mb-6">
                                <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                                    <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                    </svg>
                                </div>
                                <h2 className="text-xl font-semibold text-gray-900">Imagen del Producto</h2>
                            </div>

                            <div
                                className={`relative border-2 border-dashed rounded-2xl p-8 text-center transition-all duration-300 ${
                                    isDragOver
                                        ? 'border-blue-400 bg-blue-50 scale-105'
                                        : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
                                } ${file ? 'border-green-400 bg-green-50' : ''}`}
                                onDragOver={handleDragOver}
                                onDragLeave={handleDragLeave}
                                onDrop={handleDrop}
                            >
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    id="file"
                                    onChange={handleFileChange}
                                    accept="image/jpeg, image/png, image/webp, image/gif"
                                    className="hidden"
                                    required
                                    disabled={isSubmitting}
                                />

                                {!file ? (
                                    <div className="space-y-4">
                                        <div className="mx-auto w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center">
                                            <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="text-lg font-medium text-gray-900 mb-2">
                                                Arrastra tu imagen aquí o{' '}
                                                <button
                                                    type="button"
                                                    onClick={handleUploadClick}
                                                    className="text-blue-600 hover:text-blue-700 font-semibold underline"
                                                    disabled={isSubmitting}
                                                >
                                                    haz clic para seleccionar
                                                </button>
                                            </p>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                                            <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="text-lg font-medium text-green-900 mb-2">
                                                ¡Imagen seleccionada!
                                            </p>
                                            <p className="text-sm text-gray-500 mb-4">
                                                {file.name}
                                            </p>
                                        </div>
                                    </div>
                                )}
                            </div>

                            {previewUrl && (
                                <div className="flex justify-center">
                                    <img
                                        src={previewUrl}
                                        alt="Vista previa"
                                        className="max-h-64 w-auto rounded-xl shadow-lg border border-gray-200"
                                    />
                                </div>
                            )}
                        </div>

                        <div className="pt-6">
                            <button
                                type="submit"
                                disabled={isSubmitting || !file || !name || !price}
                                className="w-full flex items-center justify-center px-6 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-semibold rounded-xl shadow-lg hover:from-blue-700 disabled:opacity-50"
                            >
                                {isSubmitting ? 'Creando...' : 'Crear Producto'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AdminPage;