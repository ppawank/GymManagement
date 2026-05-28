import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchService } from '../services/searchService';
import './Dashboard.css';
import './SearchPortal.css';

const indexOptions = [
    { value: 'all', label: 'All' },
    { value: 'members', label: 'Members' },
    { value: 'trainers', label: 'Trainers' },
    { value: 'classes', label: 'Classes' },
    { value: 'subscriptions', label: 'Subscriptions' },
    { value: 'payments', label: 'Payments' },
    { value: 'equipment_usage', label: 'Equipment' }
];

function SearchPortal() {
    const navigate = useNavigate();
    const [query, setQuery] = useState('');
    const [index, setIndex] = useState('all');
    const [results, setResults] = useState([]);
    const [health, setHealth] = useState('UNKNOWN');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        searchService.health()
            .then((data) => setHealth(data.elasticsearch || 'UNKNOWN'))
            .catch(() => setHealth('DOWN'));
    }, []);

    const runSearch = async (event) => {
        event.preventDefault();
        if (!query.trim()) {
            setResults([]);
            return;
        }

        setLoading(true);
        setError('');

        try {
            const data = index === 'all'
                ? await searchService.searchAll(query.trim(), 30)
                : await searchService.searchIndex(index, query.trim(), 30);
            setResults(data);
        } catch (err) {
            setError(err.response?.data?.message || 'Search failed');
        } finally {
            setLoading(false);
        }
    };

    const titleFor = (result) => (
        result.name ||
        result.memberName ||
        result.equipment_name ||
        result.plan_name ||
        `Record ${result._id || ''}`
    );

    const detailFor = (result) => {
        const parts = [
            result.email,
            result.specialty,
            result.location,
            result.status,
            result.availability_status,
            result.payment_month && result.payment_year ? `${result.payment_month}/${result.payment_year}` : null
        ].filter(Boolean);
        return parts.join(' · ');
    };

    return (
        <div className="ops-page">
            <header className="ops-header">
                <div>
                    <p className="eyebrow">Search portal</p>
                    <h1>Instant Operations Search</h1>
                    <p className="subtle">Elasticsearch status: {health}</p>
                </div>
                <div className="header-actions">
                    <button className="secondary-button" onClick={() => navigate('/dashboard')}>Dashboard</button>
                </div>
            </header>

            {error && <div className="notice error">{error}</div>}

            <main className="ops-layout">
                <section className="search-panel">
                    <form className="search-form" onSubmit={runSearch}>
                        <input
                            value={query}
                            onChange={(event) => setQuery(event.target.value)}
                            placeholder="Search members, trainers, classes, subscriptions..."
                        />
                        <select value={index} onChange={(event) => setIndex(event.target.value)}>
                            {indexOptions.map((option) => (
                                <option value={option.value} key={option.value}>{option.label}</option>
                            ))}
                        </select>
                        <button className="primary-button" type="submit" disabled={loading}>
                            {loading ? 'Searching' : 'Search'}
                        </button>
                    </form>
                </section>

                <section className="results-grid">
                    {results.map((result, position) => (
                        <article className="result-card" key={`${result._index}-${result._id}-${position}`}>
                            <div className="result-topline">
                                <span>{result._index || 'record'}</span>
                                <b>{result._score ? Number(result._score).toFixed(2) : '0.00'}</b>
                            </div>
                            <h2>{titleFor(result)}</h2>
                            <p>{detailFor(result) || 'No summary fields available'}</p>
                            <pre>{JSON.stringify(result, null, 2)}</pre>
                        </article>
                    ))}
                </section>

                {!loading && query && results.length === 0 && (
                    <p className="empty-text">No results found for this query.</p>
                )}
            </main>
        </div>
    );
}

export default SearchPortal;
