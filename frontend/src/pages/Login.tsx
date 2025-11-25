import AuthCard from '../components/AuthCard';

interface Props {
  onSwitchToAdmin: () => void;
}

export default function Login({ onSwitchToAdmin }: Props) {
  return <AuthCard role="NINJA" onSwitchToAdmin={onSwitchToAdmin} />;
}
