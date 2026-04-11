import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Sparkles, Brain, Heart } from 'lucide-angular';

@Component({
  selector: 'app-ai-discovery',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-discovery.component.html',
  styleUrls: ['./ai-discovery.component.css'],
})
export class AiDiscoveryComponent {
  readonly SparklesIcon = Sparkles;
  readonly BrainIcon = Brain;
  readonly HeartIcon = Heart;
}


