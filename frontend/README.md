# Digital Bank Frontend

A modern React-based frontend application for the Digital Bank portfolio project. This application provides a comprehensive interface for managing customers, accounts, and transactions in a microservices banking architecture.

## Features

- **Dashboard**: Real-time service health monitoring and system overview
- **Customer Management**: Create and manage customer profiles
- **Account Management**: Open and manage bank accounts
- **Transaction Management**: Process deposits, withdrawals, and transfers
- **Responsive Design**: Optimized for desktop and mobile devices
- **Service Health Monitoring**: Real-time status of backend microservices

## Technology Stack

- **Frontend Framework**: React 18 with TypeScript
- **Build Tool**: Vite 5
- **Styling**: Tailwind CSS 3
- **Routing**: React Router DOM
- **State Management**: TanStack Query (React Query)
- **Icons**: Lucide React
- **Notifications**: React Hot Toast
- **Containerization**: Docker with multi-stage builds
- **Web Server**: Nginx (production)
- **Orchestration**: Kubernetes

## Project Structure

```
frontend/
├── src/
│   ├── components/     # Reusable UI components
│   ├── pages/          # Page-level components
│   ├── services/       # API service layer
│   ├── types/          # TypeScript type definitions
│   ├── App.tsx         # Main application component
│   ├── main.tsx        # Application entry point
│   └── index.css       # Global styles
├── k8s/                # Kubernetes manifests
├── Dockerfile          # Container configuration
├── nginx.conf          # Nginx configuration
├── package.json        # Dependencies and scripts
├── vite.config.ts      # Vite configuration
├── tailwind.config.js  # Tailwind CSS configuration
└── tsconfig.json       # TypeScript configuration
```

## Quick Start

### Prerequisites

- Node.js 18+
- npm 9+

### Local Development

1. **Install dependencies**
   ```bash
   npm install
   ```

2. **Start development server**
   ```bash
   npm run dev
   ```

3. **Open browser**
   Navigate to `http://localhost:3000`

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Environment Configuration

The application uses environment variables for configuration:

### Development (.env)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_ENV=development
```

### Production (.env.production)
```env
VITE_API_BASE_URL=/api
VITE_ENV=production
```

## Docker Deployment

### Build Image
```bash
docker build -t digital-bank-frontend .
```

### Run Container
```bash
docker run -p 80:80 digital-bank-frontend
```

## Kubernetes Deployment

### Apply Kubernetes Manifests
```bash
kubectl apply -f k8s/frontend-deployment.yaml
```

### Access Application
Add to `/etc/hosts`:
```
127.0.0.1 digital-bank.local
```

Access via: `http://digital-bank.local`

## API Integration

The frontend integrates with the following backend services:

- **Customer Service**: `/customers` - Customer management
- **Account Service**: `/accounts` - Account operations
- **Transaction Service**: `/transactions` - Financial transactions
- **Auth Service**: `/auth` - Authentication

### Health Check Endpoints
- Customer Service: `/customers/health`
- Account Service: `/accounts/health`
- Transaction Service: `/transactions/health`
- Auth Service: `/auth/health`

## Architecture Highlights

### Microservices Integration
- Service discovery through API Gateway
- Health monitoring with real-time status updates
- Error handling and retry mechanisms

### Responsive Design
- Mobile-first approach
- Tailwind CSS utility classes
- Consistent design system

### Performance Optimization
- Code splitting with React.lazy()
- Image optimization
- Nginx caching configuration

## Development Guidelines

### Component Structure
- Use functional components with hooks
- Implement TypeScript interfaces for props
- Follow React best practices

### Styling
- Use Tailwind CSS classes
- Follow BEM naming convention for custom styles
- Maintain consistent spacing and colors

### State Management
- Use TanStack Query for server state
- Use React hooks for local state
- Implement proper loading and error states

## Production Deployment

### Build Optimization
- Tree shaking and code splitting
- Minification and compression
- Asset optimization

### Security
- Content Security Policy headers
- XSS protection
- Secure headers configuration

### Monitoring
- Health check endpoints
- Error tracking
- Performance monitoring

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is part of a portfolio demonstration. See the main project repository for licensing information.